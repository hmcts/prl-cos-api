package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.Annex_1_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.Annex_1_FILENAME_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_BLANK_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_BLANK_DOCUMENT_WELSH_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C7_BLANK_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ENG_STATIC_DOCS_PATH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PRIVACY_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PRIVACY_DOCUMENT_FILENAME_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_C9_PERSONAL_SERVICE_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_FAMILY_PRESIDENTS_NOTE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_FAMILY_PRESIDENTS_NOTE_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_FL415_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MULTIPART_FILE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.THIS_INFORMATION_IS_CONFIDENTIAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.DocumentUtils.toGeneratedDocumentInfo;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"java:S6204"})
public class ServiceOfApplicationPostService {
    private final BulkPrintService bulkPrintService;
    private final DocumentGenService documentGenService;
    private final DocumentLanguageService documentLanguageService;
    private final DgsService dgsService;
    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final LaunchDarklyClient launchDarklyClient;
    private static final String LETTER_TYPE = "ApplicationPack";

    public BulkPrintDetails sendPostNotificationToParty(CaseData caseData,
                                                        String authorisation,
                                                        Element<PartyDetails> party,
                                                        List<Document> docs,
                                                        String servedParty) {
        // Sends post
        return sendBulkPrint(caseData, authorisation, docs, party, servedParty);
    }

    public List<Document> getCoverSheets(CaseData caseData, String auth, Address address, String name, String coverSheetTemplate) throws Exception {
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        List<Document> coversheets = new ArrayList<>();
        if (null != address && null != address.getAddressLine1()) {
            if (documentLanguage.isGenEng()) {
                GeneratedDocumentInfo generatedDocumentInfo = fetchCoverSheetBasedOnLanguagePreference(caseData, auth, address, name, false,
                                                                                 coverSheetTemplate);
                coversheets.add(DocumentUtils.toCoverSheetDocument(generatedDocumentInfo));
            }
            if (documentLanguage.isGenWelsh()) {
                GeneratedDocumentInfo generatedDocumentInfo = fetchCoverSheetBasedOnLanguagePreference(caseData, auth, address, name, true,
                                                                                 coverSheetTemplate);
                coversheets.add(DocumentUtils.toCoverSheetDocument(generatedDocumentInfo));
            }
        } else {
            log.error("ADDRESS NOT PRESENT, CAN NOT GENERATE COVER LETTER");
        }
        return coversheets;
    }

    private GeneratedDocumentInfo fetchCoverSheetBasedOnLanguagePreference(CaseData caseData, String auth,
                                                                           Address address, String name,
                                                                           boolean isWelsh, String coverSheetTemplate) throws Exception {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("coverPagePartyName", null != name ? name : " ");
        dataMap.put("coverPageAddress", address);
        dataMap.put("id", String.valueOf(caseData.getId()));
        GeneratedDocumentInfo generatedDocumentInfo;
        generatedDocumentInfo = dgsService.generateDocument(
            auth, String.valueOf(caseData.getId()),
            documentGenService.getTemplate(
                caseData,
                coverSheetTemplate, isWelsh
            ), dataMap
        );
        return generatedDocumentInfo;
    }

    public List<Document> getStaticDocs(String auth, String caseType, CaseData caseData) {
        List<Document> generatedDocList = new ArrayList<>();
        UploadResponse uploadResponse = null;
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if (C100_CASE_TYPE.equalsIgnoreCase(caseType)) {
            List<MultipartFile> files = new ArrayList<>();
            if (documentLanguage.isGenEng()) {
                files.add(new InMemoryMultipartFile(
                    SOA_MULTIPART_FILE,
                    PRIVACY_DOCUMENT_FILENAME,
                    APPLICATION_PDF_VALUE,
                    DocumentUtils.readBytes(URL_STRING + ENG_STATIC_DOCS_PATH + PRIVACY_DOCUMENT_FILENAME)
                ));
            }
            if (documentLanguage.isGenWelsh()) {
                files.add(new InMemoryMultipartFile(
                    SOA_MULTIPART_FILE,
                    PRIVACY_DOCUMENT_FILENAME_WELSH,
                    APPLICATION_PDF_VALUE,
                    DocumentUtils.readBytes(URL_STRING + ENG_STATIC_DOCS_PATH + PRIVACY_DOCUMENT_FILENAME_WELSH)
                ));
            }
            // FPET-1056 Annex 1 file inclusion
            if (Objects.nonNull(caseData.getServiceOfApplication()) && YesOrNo.Yes.equals(caseData.getServiceOfApplication().getIsConfidential())) {
                if (documentLanguage.isGenEng()) {
                    files.add(new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        Annex_1_FILENAME,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(URL_STRING + ENG_STATIC_DOCS_PATH + Annex_1_FILENAME)
                    ));
                }
                if (documentLanguage.isGenWelsh()) {
                    files.add(new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        Annex_1_FILENAME_WELSH,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(URL_STRING + ENG_STATIC_DOCS_PATH + Annex_1_FILENAME_WELSH)
                    ));
                }
            }
            //PRL-5360 - Remove mediation voucher & add new President note
            if (documentLanguage.isGenEng()) {
                files.add(new InMemoryMultipartFile(
                    SOA_MULTIPART_FILE,
                    SOA_FAMILY_PRESIDENTS_NOTE,
                    APPLICATION_PDF_VALUE,
                    DocumentUtils.readBytes(URL_STRING + ENG_STATIC_DOCS_PATH + SOA_FAMILY_PRESIDENTS_NOTE)
                ));
            }
            if (documentLanguage.isGenWelsh()) {
                files.add(new InMemoryMultipartFile(
                    SOA_MULTIPART_FILE,
                    SOA_FAMILY_PRESIDENTS_NOTE_WELSH,
                    APPLICATION_PDF_VALUE,
                    DocumentUtils.readBytes(URL_STRING + ENG_STATIC_DOCS_PATH + SOA_FAMILY_PRESIDENTS_NOTE_WELSH)
                ));
            }
            files.addAll(
                List.of(
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        C7_BLANK_DOCUMENT_FILENAME,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(URL_STRING + ENG_STATIC_DOCS_PATH + C7_BLANK_DOCUMENT_FILENAME)
                    ),
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        SOA_C9_PERSONAL_SERVICE_FILENAME,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(URL_STRING + ENG_STATIC_DOCS_PATH + SOA_C9_PERSONAL_SERVICE_FILENAME)
                    )
                )
            );
            if (documentLanguage.isGenEng()) {
                files.add(
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        C1A_BLANK_DOCUMENT_FILENAME,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(URL_STRING + ENG_STATIC_DOCS_PATH + C1A_BLANK_DOCUMENT_FILENAME)
                    ));
            }
            if (documentLanguage.isGenWelsh()) {
                files.add(
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        C1A_BLANK_DOCUMENT_WELSH_FILENAME,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(URL_STRING + ENG_STATIC_DOCS_PATH + C1A_BLANK_DOCUMENT_WELSH_FILENAME)
                    ));
            }

            uploadResponse = caseDocumentClient.uploadDocuments(
                auth,
                authTokenGenerator.generate(),
                PrlAppsConstants.CASE_TYPE,
                PrlAppsConstants.JURISDICTION,
                files
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
                        DocumentUtils.readBytes(URL_STRING + ENG_STATIC_DOCS_PATH + PRIVACY_DOCUMENT_FILENAME)
                    ),
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        SOA_FL415_FILENAME,
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes(URL_STRING + ENG_STATIC_DOCS_PATH + SOA_FL415_FILENAME)
                    )
                )
            );
        }
        if (null != uploadResponse) {
            List<Document> uploadedStaticDocs = uploadResponse.getDocuments().stream().map(DocumentUtils::toPrlDocument).toList();
            generatedDocList.addAll(uploadedStaticDocs);
            return generatedDocList;
        }
        return Collections.emptyList();
    }

    public CaseData getRespondentCaseData(PartyDetails partyDetails, CaseData caseData) {
        return CaseData
            .builder()
            .id(caseData.getId())
            .respondents(List.of(element(partyDetails)))
            .build();
    }

    public List<GeneratedDocumentInfo> getUploadedDocumentsServiceOfApplication(CaseData caseData) {
        List<GeneratedDocumentInfo> docs = new ArrayList<>();
        Optional<Document> pd36qLetter = Optional.ofNullable(caseData.getServiceOfApplicationUploadDocs().getPd36qLetter());
        Optional<Document> specialArrangementLetter = Optional.ofNullable(caseData.getServiceOfApplicationUploadDocs()
                                                                              .getSpecialArrangementsLetter());
        pd36qLetter.ifPresent(document -> docs.add(toGeneratedDocumentInfo(document)));
        specialArrangementLetter.ifPresent(document -> docs.add(toGeneratedDocumentInfo(document)));
        return docs;
    }

    public Document getFinalDocument(CaseData caseData) {
        if (!welshCase(caseData)) {
            return caseData.getFinalDocument();
        }
        return caseData.getFinalWelshDocument();
    }

    public Optional<Document> getC1aDocument(CaseData caseData) {
        if (hasAllegationsOfHarm(caseData)) {
            if (!welshCase(caseData)) {
                return Optional.of(caseData.getC1ADocument());
            }
            return Optional.of(caseData.getC1AWelshDocument());
        }
        return Optional.empty();
    }

    private boolean welshCase(CaseData caseData) {
        return caseData.getFinalWelshDocument() != null;
    }

    private boolean hasAllegationsOfHarm(CaseData caseData) {
        return Yes.equals(caseData.getAllegationOfHarm().getAllegationsOfHarmYesNo());
    }

    private BulkPrintDetails sendBulkPrint(CaseData caseData, String authorisation,
                                           List<Document> docs, Element<PartyDetails> party, String servedParty) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String currentDate = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);
        String bulkPrintedId = "";
        PartyDetails partyDetails = party.getValue();
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
                    partyDetails.getLabelForDynamicList()
                );
                log.info("ID in the queue from bulk print service : {}", bulkPrintId);
                bulkPrintedId = String.valueOf(bulkPrintId);
            }
        } catch (Exception e) {
            log.error("The bulk print service has failed", e);
        }
        Address address = Yes.equals(partyDetails.getIsAddressConfidential())
            ? Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build()
            : partyDetails.getAddress();

        return BulkPrintDetails.builder()
            .bulkPrintId(bulkPrintedId)
            .servedParty(servedParty)
            .printedDocs(String.join(",", docs.stream().map(Document::getDocumentFileName).toList()))
            .recipientsName(partyDetails.getLabelForDynamicList())
            .printDocs(docs.stream().map(ElementUtils::element).toList())
            .postalAddress(address)
            .timeStamp(currentDate)
            .partyIds(String.valueOf(party.getId()))
            .build();
    }

}
