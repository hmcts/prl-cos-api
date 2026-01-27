package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.exception.InvalidResourceException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.PoiTlDocxRenderer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Service for handling custom order document operations.
 * Uses CaseDocumentClient for document access.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CustomOrderService {

    private final ObjectMapper objectMapper;
    private final AuthTokenGenerator authTokenGenerator;
    private final HearingDataService hearingDataService;
    private final PoiTlDocxRenderer poiTlDocxRenderer;
    private final UploadDocumentService uploadService;
    private final CaseDocumentClient caseDocumentClient;
    private final SystemUserService systemUserService;

    @Value("${case_document_am.url}")
    private String cdamBaseUrl;

    /**
     * Renders an uploaded custom order template with case data placeholders and stores it.
     *
     * @param authorisation User authorization token (used for upload)
     * @param caseId The case ID
     * @param caseData The case data
     * @param caseDataUpdated Map to store updated case data
     * @param populateJudgeNames Function to populate judge names on case data
     * @param populatePartyDetails Function to populate party details on case data
     * @return Updated case data map with rendered document
     * @throws IOException If document processing fails
     */
    public Map<String, Object> renderUploadedCustomOrderAndStoreOnManageOrders(
        String authorisation,
        Long caseId,
        CaseData caseData,
        Map<String, Object> caseDataUpdated,
        java.util.function.Function<CaseData, CaseData> populateJudgeNames,
        java.util.function.Function<CaseData, CaseData> populatePartyDetails
    ) throws IOException {
        log.info("into render with caseId: {}", caseId);

        // Read customOrderDoc from raw map (not in CaseData model due to constructor param limit)
        uk.gov.hmcts.reform.prl.models.documents.Document templateDoc = objectMapper.convertValue(
            caseDataUpdated.get("customOrderDoc"),
            uk.gov.hmcts.reform.prl.models.documents.Document.class
        );
        if (templateDoc == null || templateDoc.getDocumentBinaryUrl() == null) {
            throw new IllegalArgumentException("customOrderDoc is missing from case data");
        }

        // 1) Download template bytes using user's auth token
        byte[] templateBytes;
        String documentUrl = templateDoc.getDocumentBinaryUrl();
        log.info("Document URL: {}", documentUrl);

        templateBytes = downloadFromBinaryUrl(documentUrl, authorisation, authTokenGenerator.generate());
        log.info("Successfully downloaded document, size: {} bytes", templateBytes != null ? templateBytes.length : 0);

        // 2) Build placeholders (minimal POC + names/solicitors if possible)
        Map<String, Object> data = buildCustomOrderPlaceholders(caseId, caseData);

        log.info("Custom order placeholders from buildCustomOrderPlaceholders: {}", data);

        caseData = populateJudgeNames.apply(caseData);
        caseData = populatePartyDetails.apply(caseData);

        // Get party names - this puts data under "tempPartyNamesForDocGen" key
        Map<String, Object> temp = new HashMap<>();
        hearingDataService.populatePartiesAndSolicitorsNames(caseData, temp);

        // Flatten the nested map into our data map
        @SuppressWarnings("unchecked")
        Map<String, Object> partyNames = (Map<String, Object>) temp.get("tempPartyNamesForDocGen");
        if (partyNames != null) {
            data.putAll(partyNames);
            log.info("Added party names to placeholders: {}", partyNames.keySet());
        }

        log.info("Final placeholder keys for custom order: {}", data.keySet());

        // 3) Render filled docx
        byte[] filledDocxBytes = poiTlDocxRenderer.render(templateBytes, data);

        // 4) Upload rendered docx
        String orderName = (caseData.getNameOfOrder() != null
            && !caseData.getNameOfOrder().isBlank())
            ? caseData.getNameOfOrder()
            : "custom_order";

        String outName = orderName + "_" + caseId + ".docx";
        log.info("before upload doc of rendered doc: {}", outName);
        uk.gov.hmcts.reform.ccd.document.am.model.Document uploaded =
            uploadService.uploadDocument(
                filledDocxBytes,
                outName,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                authorisation
            );

        uk.gov.hmcts.reform.prl.models.documents.Document renderedDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentBinaryUrl(uploaded.links.binary.href)
                .documentUrl(uploaded.links.self.href)
                .documentFileName(uploaded.originalDocumentName)
                .documentCreatedOn(uploaded.createdOn)
                .build();

        // 5) Store the transformed doc
        caseDataUpdated.put("customOrderTransformedDoc", renderedDoc);

        return caseDataUpdated;
    }

    public byte[] downloadFromBinaryUrl(String binaryUrl, String userAuth, String s2sAuth) {
        String fileName = FilenameUtils.getName(binaryUrl);
        //String testUrl = "http://dm-store-aat.service.core-compute-aat.internal/documents/c577e97b-d587-4bc8-8012-891d664e6388/binary";
        log.info("Downloading from dm-store binary URL temp: {}", binaryUrl);
        //String systemAuthorisation = systemUserService.getSysUserToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", userAuth);
        headers.set("ServiceAuthorization", s2sAuth);

        try {
            ResponseEntity<byte[]> response = new RestTemplate().exchange(
                binaryUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class
            );

            byte[] body = response.getBody();
            if (body == null) {
                throw new InvalidResourceException("Empty body for " + fileName);
            }
            return body;
        } catch (Exception e) {
            log.error(e.getMessage(),e.toString());
            throw new InvalidResourceException("Doc name " + fileName, e);
        }
    }

    public byte[] getDocumentBytesLikeDocumentGenService(String docUrl, String authToken, String s2sToken) {

        String fileName = FilenameUtils.getName(docUrl);
        UUID documentId = extractUuidFromUrl(docUrl);

        // Call CDAM directly via RestTemplate to bypass permission wrapper
        String cdamUrl = cdamBaseUrl + "/cases/documents/" + documentId + "/binary";
        log.info("Calling CDAM directly at: {}", cdamUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.set("ServiceAuthorization", s2sToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.exchange(
                cdamUrl,
                HttpMethod.GET,
                entity,
                byte[].class
            );
            byte[] body = response.getBody();
            log.info("Successfully downloaded document from CDAM, size: {} bytes", body != null ? body.length : 0);
            return body;
        } catch (Exception e) {
            log.error("Failed to download from CDAM: {}", e.getMessage());
            throw new InvalidResourceException("Doc name " + fileName, e);
        }
    }

    private UUID extractUuidFromUrl(String url) {
        // Handle both formats:
        // http://dm-store/documents/{uuid}
        // http://dm-store/documents/{uuid}/binary
        String cleanUrl = url.endsWith("/binary") ? url.substring(0, url.length() - 7) : url;
        return UUID.fromString(cleanUrl.substring(cleanUrl.lastIndexOf('/') + 1));
    }

    /**
     * Downloads document bytes using the user's authorization token via CaseDocumentClient.
     *
     * @param binaryUrl The document binary URL
     * @param authorisation User's authorization token
     * @return The document bytes
     */
    private byte[] getDocumentBytes(String binaryUrl, String authorisation) {
        String serviceToken = authTokenGenerator.generate();

        log.info("Downloading document from URL: {}", binaryUrl);

        try {
            var response = caseDocumentClient.getDocumentBinary(
                authorisation,
                serviceToken,
                binaryUrl
            );

            if (response.getBody() != null) {
                byte[] bytes = response.getBody().getInputStream().readAllBytes();
                log.info("Successfully downloaded document, size: {} bytes", bytes.length);
                return bytes;
            }
            throw new RuntimeException("Document body is null");
        } catch (IOException e) {
            log.error("Failed to read document bytes: {}", e.getMessage());
            throw new RuntimeException("Failed to read document", e);
        }
    }

    /**
     * Safely builds placeholder map for custom order template.
     * Returns empty string for any null values or exceptions.
     */
    private Map<String, Object> buildCustomOrderPlaceholders(Long caseId, CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        log.info("Building custom order placeholders for caseId: {}", caseId);

        // Use caseId directly from controller (caseData.getId() may not be populated)
        data.put("caseNumber", String.valueOf(caseId));
        log.info("Placeholder 'caseNumber' = '{}'", caseId);
        safePut(data, "courtName", caseData::getCourtName);
        safePut(data, "respondent1Name", () -> {
            var respondent = caseData.getRespondents().getFirst().getValue();
            return (respondent.getFirstName() + " " + respondent.getLastName()).trim();
        });
        safePut(data, "hearingDate", () ->
            caseData.getManageOrders().getOrdersHearingDetails().getLast()
        );

        return data;
    }

    private void safePut(Map<String, Object> map, String key, Supplier<Object> valueSupplier) {
        try {
            Object value = valueSupplier.get();
            if (value != null && !"null".equals(String.valueOf(value)) && !"0".equals(String.valueOf(value))) {
                map.put(key, value);
                log.info("Placeholder '{}' = '{}'", key, value);
            } else {
                map.put(key, "");
                log.warn("Placeholder '{}' resolved to null/0/empty, using empty string. Raw value was: {}", key, value);
            }
        } catch (Exception e) {
            map.put(key, "");
            log.warn("Placeholder '{}' failed to resolve: {}", key, e.getMessage());
        }
    }
}
