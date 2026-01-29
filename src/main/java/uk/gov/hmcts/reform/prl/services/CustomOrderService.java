package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.document.PoiTlDocxRenderer;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Service for handling custom order document operations.
 * Uses DocumentGenService for document access.
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
    private final DocumentGenService documentGenService;
    private final SystemUserService systemUserService;
    private final AllTabServiceImpl allTabService;

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

        // 1) First persist the document to the case so CDAM associates it
        // This is necessary because CDAM requires case-level permissions for document access
        String documentUrl = templateDoc.getDocumentBinaryUrl();
        log.info("Document URL: {}", documentUrl);

        associateDocumentWithCase(String.valueOf(caseId));
        log.info("Document associated with case, now downloading...");

        // 2) Download template bytes using DocumentGenService (now works because doc is case-associated)
        byte[] templateBytes;
        String systemAuthorisation = systemUserService.getSysUserToken();
        templateBytes = documentGenService.getDocumentBytes(documentUrl, systemAuthorisation, authTokenGenerator.generate());
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
        // Remove the original uploaded doc after transformation
        caseDataUpdated.remove("customOrderDoc");
        return caseDataUpdated;
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

    /**
     * Associates the document with the case by submitting a minimal UPDATE_ALL_TABS event.
     * This triggers CDAM to recognize the document belongs to this case, allowing subsequent downloads.
     *
     * Note: We intentionally don't add the document to case data here - just triggering the event
     * with the existing case data seems to be enough to establish CDAM association for documents
     * uploaded during this case's event flow.
     */
    private void associateDocumentWithCase(String caseId) {
        log.info("Triggering UPDATE_ALL_TABS for case {} to establish CDAM association", caseId);

        StartAllTabsUpdateDataContent startContent = allTabService.getStartUpdateForSpecificEvent(
            caseId,
            CaseEvent.UPDATE_ALL_TABS.getValue()
        );

        // Submit with existing case data - don't add the document, just trigger the event
        // This may be enough to establish CDAM association for pending documents
        allTabService.submitAllTabsUpdate(
            startContent.authorisation(),
            caseId,
            startContent.startEventResponse(),
            startContent.eventRequestData(),
            startContent.caseDataMap()
        );

        log.info("UPDATE_ALL_TABS completed, CDAM association should now be established");
    }
}
