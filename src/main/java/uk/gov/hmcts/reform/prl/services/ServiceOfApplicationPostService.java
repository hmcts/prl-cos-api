package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_BLANK_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C7_BLANK_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ENG_STATIC_DOCS_PATH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PRIVACY_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_C9_PERSONAL_SERVICE_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_FL415_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_FL416_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MEDIATION_VOUCHER_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MULTIPART_FILE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_NOTICE_SAFETY;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"java:S6204"})
public class ServiceOfApplicationPostService {

    @Autowired
    private final BulkPrintService bulkPrintService;

    @Autowired
    private final DocumentGenService documentGenService;

    @Autowired
    private final DocumentLanguageService documentLanguageService;

    @Autowired
    private final DgsService dgsService;

    @Autowired
    private final CaseDocumentClient caseDocumentClient;

    @Autowired
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    private final LaunchDarklyClient launchDarklyClient;

    private static final String LETTER_TYPE = "ApplicationPack";


    public BulkPrintDetails sendPostNotificationToParty(CaseData caseData,
                                                        String authorisation,
                                                        PartyDetails partyDetails,
                                                        List<Document> docs, String servedParty) {
        // Sends post
        return sendBulkPrint(caseData, authorisation, docs, partyDetails.getAddress(),
                             partyDetails.getLabelForDynamicList(), servedParty
        );
    }

    public GeneratedDocumentInfo getCoverLetterGeneratedDocInfo(CaseData caseData, String auth, Address address, String name) throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo = null;
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if (null != address && null != address.getAddressLine1()) {
            generatedDocumentInfo = dgsService.generateDocument(
                auth,

                CaseDetails.builder().caseData(caseData.toBuilder().serviceOfApplication(
                    ServiceOfApplication.builder().coverPageAddress(Address.builder()
                                                                        .addressLine1(address.getAddressLine1())
                                                                        .addressLine3(address.getAddressLine3())
                                                                        .county(address.getCounty())
                                                                        .postCode(address.getPostCode())
                                                                        .postTown(address.getPostTown())
                                                                        .build())
                        .coverPagePartyName(null != name ? name : " ").build()
                ).build()).build(),
                documentGenService.getTemplate(
                    caseData,
                    DOCUMENT_COVER_SHEET_HINT,
                    documentLanguage.isGenEng() ? Boolean.FALSE : Boolean.TRUE
                ));
        } else {
            log.error("ADDRESS NOT PRESENT, CAN NOT GENERATE COVER LETTER");
        }
        return generatedDocumentInfo;
    }

    public Document getCoverLetter(CaseData caseData, String auth, Address address, String name) throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo = null;
        Map<String, Object> dataMap = new HashMap<>();
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if (null != address && null != address.getAddressLine1()) {
            dataMap.put("coverPagePartyName", name);
            dataMap.put("coverPageAddress", address);
            dataMap.put("id", String.valueOf(caseData.getId()));
            generatedDocumentInfo = dgsService.generateDocument(
                auth, String.valueOf(caseData.getId()),
                documentGenService.getTemplate(
                    caseData,
                    DOCUMENT_COVER_SHEET_HINT, documentLanguage.isGenEng() ? Boolean.FALSE : Boolean.TRUE), dataMap);
        } else {
            log.error("ADDRESS NOT PRESENT, CAN NOT GENERATE COVER LETTER");
        }
        return DocumentUtils.toCoverLetterDocument(generatedDocumentInfo);
    }

    public List<Document> getStaticDocs(String auth, CaseData caseData) {
        List<Document> generatedDocList = new ArrayList<>();
        UploadResponse uploadResponse = null;
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            uploadResponse = caseDocumentClient.uploadDocuments(
                auth,
                authTokenGenerator.generate(),
                PrlAppsConstants.CASE_TYPE,
                PrlAppsConstants.JURISDICTION,
                List.of(
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        PRIVACY_DOCUMENT_FILENAME,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(ENG_STATIC_DOCS_PATH + PRIVACY_DOCUMENT_FILENAME)
                    ),
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        SOA_MEDIATION_VOUCHER_FILENAME,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(ENG_STATIC_DOCS_PATH + SOA_MEDIATION_VOUCHER_FILENAME)
                    ),
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        SOA_NOTICE_SAFETY,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(ENG_STATIC_DOCS_PATH + SOA_NOTICE_SAFETY)
                    ),
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        C7_BLANK_DOCUMENT_FILENAME,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(ENG_STATIC_DOCS_PATH + C7_BLANK_DOCUMENT_FILENAME)
                    ),
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        SOA_C9_PERSONAL_SERVICE_FILENAME,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(ENG_STATIC_DOCS_PATH + SOA_C9_PERSONAL_SERVICE_FILENAME)
                    ),
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        C1A_BLANK_DOCUMENT_FILENAME,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(ENG_STATIC_DOCS_PATH + C1A_BLANK_DOCUMENT_FILENAME)
                    )
                )
            );
        } else {

            uploadResponse = caseDocumentClient.uploadDocuments(
                auth,
                authTokenGenerator.generate(),
                PrlAppsConstants.CASE_TYPE,
                PrlAppsConstants.JURISDICTION,
                List.of(
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        PRIVACY_DOCUMENT_FILENAME,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(ENG_STATIC_DOCS_PATH + PRIVACY_DOCUMENT_FILENAME)
                    ),
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        SOA_FL416_FILENAME,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(ENG_STATIC_DOCS_PATH + SOA_FL416_FILENAME)
                    ),
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        SOA_FL415_FILENAME,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(ENG_STATIC_DOCS_PATH + SOA_FL415_FILENAME)
                    )
                )
            );
        }
        if (null != uploadResponse) {
            List<Document> uploadedStaticDocs = uploadResponse.getDocuments().stream().map(DocumentUtils::toPrlDocument).collect(
                Collectors.toList());
            generatedDocList.addAll(uploadedStaticDocs);
            return generatedDocList;
        }
        return Collections.emptyList();
    }

    private BulkPrintDetails sendBulkPrint(CaseData caseData, String authorisation,
                                          List<Document> docs, Address address, String name, String servedParty) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String currentDate = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);
        String bulkPrintedId = "";
        try {
            log.info("*** Initiating request to Bulk print service ***");
            log.info("*** number of files in the pack *** {}", null != docs ? docs.size() : "empty");
            if (launchDarklyClient.isFeatureEnabled("soa-bulk-print")) {
                log.info("******Bulk print is enabled****");
                UUID bulkPrintId = bulkPrintService.send(
                        String.valueOf(caseData.getId()),
                        authorisation,
                        LETTER_TYPE,
                        docs,
                        name
                );
                log.info("ID in the queue from bulk print service : {}", bulkPrintId);
                bulkPrintedId = String.valueOf(bulkPrintId);
            }
        } catch (Exception e) {
            log.error("The bulk print service has failed", e);
        }
        return BulkPrintDetails.builder()
            .bulkPrintId(bulkPrintedId)
            .servedParty(servedParty)
            .printedDocs(String.join(",", docs.stream().map(Document::getDocumentFileName).toList()))
            .recipientsName(name)
            .printDocs(docs.stream().map(ElementUtils::element).toList())
            .postalAddress(address)
            .timeStamp(currentDate).build();
    }

}
