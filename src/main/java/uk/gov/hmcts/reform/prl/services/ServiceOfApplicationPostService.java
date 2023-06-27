package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_PRIVACY_NOTICE_HINT;
import static uk.gov.hmcts.reform.prl.utils.DocumentUtils.toGeneratedDocumentInfo;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceOfApplicationPostService {

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private DocumentGenService documentGenService;

    @Autowired
    private DocumentLanguageService documentLanguageService;

    @Autowired
    private DgsService dgsService;

    @Autowired
    private CaseDocumentClient caseDocumentClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    private static final String LETTER_TYPE = "ApplicationPack";

    public List<Document> send(CaseData caseData, String authorisation) {
        // Sends post to the respondents who are not represented by a solicitor
        List<Document> sentDocs = new ArrayList<>();
        caseData.getRespondents().stream()
            .map(Element::getValue)
            .filter(partyDetails -> !YesNoDontKnow.yes.equals(partyDetails.getDoTheyHaveLegalRepresentation()))
            .filter(partyDetails -> YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown()))
            .forEach(partyDetails -> {
                try {
                    List<Document> docs = getListOfDocumentInfo(authorisation, caseData, partyDetails);
                    log.info("*** Initiating request to Bulk print service ***");
                    bulkPrintService.send(
                        String.valueOf(caseData.getId()),
                        authorisation,
                        LETTER_TYPE,
                        docs
                    );
                    sentDocs.addAll(docs);
                } catch (Exception e) {
                    log.info("The bulk print service has failed: {}", e.getMessage());
                }
            });
        return sentDocs;
    }

    public List<GeneratedDocumentInfo> sendDocs(CaseData caseData, String authorisation) {
        // Sends post to other parties
        List<GeneratedDocumentInfo> sentDocs = new ArrayList<>();
        CaseData blankCaseData = CaseData.builder().build();
        Optional<List<Element<PartyDetails>>> otherPeopleToNotify = Optional.ofNullable(caseData.getOthersToNotify());
        otherPeopleToNotify.ifPresent(elements -> elements
            .stream()
            .map(Element::getValue)
            .filter(partyDetails -> YesOrNo.Yes.getDisplayedValue()
                .equalsIgnoreCase(partyDetails.getIsCurrentAddressKnown().getDisplayedValue()))
            .forEach(partyDetails -> {
                List<GeneratedDocumentInfo> docs = null;
                docs = getUploadedDocumentsServiceOfApplication(caseData);
                try {
                    docs.add(generateDocument(authorisation, blankCaseData, DOCUMENT_PRIVACY_NOTICE_HINT));
                    //docs.add(getCoverLetterGeneratedDocInfo(caseData, authorisation));
                } catch (Exception e) {
                    log.info("*** Error while generating privacy notice to be served ***");
                }
                //sentDocs.add(sendBulkPrint(caseData, authorisation, docs, partyDetails));
            }
            ));
        return sentDocs;
    }

    public BulkPrintDetails sendPostNotificationToParty(CaseData caseData,
                                                        String authorisation,
                                                        PartyDetails partyDetails,
                                                        List<Document> docs, String servedParty) {
        // Sends post
        return sendBulkPrint(caseData, authorisation, docs, partyDetails.getAddress(),
                             partyDetails.getLabelForDynamicList(), servedParty
        );
    }

    public BulkPrintDetails sendPostNotification(CaseData caseData, String authorisation, Address address, String name,
                                                 List<Document> docs, String servedParty) {
        // Sends post
        return sendBulkPrint(caseData, authorisation, docs, address, name, servedParty);
    }


    private List<Document> getListOfDocumentInfo(String auth, CaseData caseData, PartyDetails partyDetails) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.add(getFinalDocument(caseData));
        getC1aDocument(caseData).ifPresent(docs::add);
        //docs.addAll(getSelectedOrders(caseData));
        /*docs.addAll(getUploadedDocumentsServiceOfApplication(caseData));
        CaseData blankCaseData = CaseData.builder().build();
        docs.add(generateDocument(auth, blankCaseData, DOCUMENT_PRIVACY_NOTICE_HINT));
        docs.add(generateDocument(auth, blankCaseData, DOCUMENT_C1A_BLANK_HINT));
        docs.add(generateDocument(auth, blankCaseData, DOCUMENT_C7_DRAFT_HINT));
        docs.add(generateDocument(auth, blankCaseData, DOCUMENT_C8_BLANK_HINT));*/
        return docs;
    }

    public Document getCoverLetter(String auth, Address address, CaseData caseData) throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo = null;
        //generatedDocumentInfo = getCoverLetterGeneratedDocInfo(caseData, auth);
        log.info("generatedDocumentInfo {}", generatedDocumentInfo);
        if (null != generatedDocumentInfo) {
            return Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("cover_letter.pdf").build();
        }
        return null;
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
                    documentLanguage.isGenEng() ? false : true
                ));
        } else {
            log.error("ADDRESS NOT PRESENT, CAN NOT GENERATE COVER LETTER");
        }
        return generatedDocumentInfo;
    }

    public List<Document> getStaticDocs(String auth, CaseData caseData) {
        List<Document> generatedDocList = new ArrayList<>();
        UploadResponse uploadResponse = null;
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            log.info("Time before upload{}", LocalDateTime.now());
            uploadResponse = caseDocumentClient.uploadDocuments(
                auth,
                authTokenGenerator.generate(),
                PrlAppsConstants.CASE_TYPE,
                PrlAppsConstants.JURISDICTION,
                List.of(
                    new InMemoryMultipartFile(
                        "files",
                        "Privacy_Notice.pdf",
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes("/staticdocs/Privacy_Notice.pdf")
                    ),
                    new InMemoryMultipartFile(
                        "files",
                        "Mediation-voucher.pdf",
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes("/staticdocs/Mediation-voucher.pdf")
                    ),
                    new InMemoryMultipartFile(
                        "files",
                        "Notice-safety.pdf",
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes("/staticdocs/Notice-safety.pdf")
                    ),
                    new InMemoryMultipartFile(
                        "files",
                        "Blank_C7.pdf",
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes("/staticdocs/Blank_C7.pdf")
                    ),
                    new InMemoryMultipartFile(
                        "files",
                        "C9_personal_service.pdf",
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes("/staticdocs/C9_personal_service.pdf")
                    ),
                    new InMemoryMultipartFile(
                        "files",
                        "C1_Blank.pdf",
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes("/staticdocs/C1_Blank.pdf")
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
                        "files",
                        "Privacy_Notice.pdf",
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes("/staticdocs/Privacy_Notice.pdf")
                    ),
                    new InMemoryMultipartFile(
                        "files",
                        "FL416.pdf",
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes("/staticdocs/FL416.pdf")
                    ),
                    new InMemoryMultipartFile(
                        "files",
                        "FL415.pdf",
                        APPLICATION_PDF_VALUE,
                        DocumentUtils.readBytes("/staticdocs/FL415.pdf")
                    )
                )
            );
        }
        List<Document> uploadedStaticDocs = uploadResponse.getDocuments().stream().map(DocumentUtils::toPrlDocument).collect(
            Collectors.toList());
        generatedDocList.addAll(uploadedStaticDocs);
        return generatedDocList;
    }

    private CaseData getRespondentCaseData(PartyDetails partyDetails, CaseData caseData) {
        return CaseData
            .builder()
            .id(caseData.getId())
            .respondents(List.of(element(partyDetails)))
            .build();
    }

    private List<GeneratedDocumentInfo> getUploadedDocumentsServiceOfApplication(CaseData caseData) {
        List<GeneratedDocumentInfo> docs = new ArrayList<>();
        Optional<Document> pd36qLetter = Optional.ofNullable(caseData.getServiceOfApplicationUploadDocs().getPd36qLetter());
        Optional<Document> specialArrangementLetter = Optional.ofNullable(caseData.getServiceOfApplicationUploadDocs()
                                                                              .getSpecialArrangementsLetter());
        pd36qLetter.ifPresent(document -> docs.add(toGeneratedDocumentInfo(document)));
        specialArrangementLetter.ifPresent(document -> docs.add(toGeneratedDocumentInfo(document)));
        return docs;
    }

    private Document getFinalDocument(CaseData caseData) {
        if (!welshCase(caseData)) {
            return caseData.getFinalDocument();
        }
        return caseData.getFinalWelshDocument();
    }

    private Optional<Document> getC1aDocument(CaseData caseData) {
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
        return YesOrNo.Yes.equals(caseData.getAllegationOfHarm().getAllegationsOfHarmYesNo());
    }

    private List<GeneratedDocumentInfo> getSelectedOrders(CaseData caseData) {
        List<String> orderNames = caseData.getServiceOfApplicationScreen1()
            .getValue().stream().map(DynamicMultiselectListElement::getLabel)
            .collect(Collectors.toList());

        return caseData.getOrderCollection().stream()
            .map(Element::getValue)
            .filter(i -> orderNames.contains(i.getOrderTypeId()))
            .map(i -> toGeneratedDocumentInfo(i.getOrderDocument()))
            .collect(Collectors.toList());
    }

    private GeneratedDocumentInfo generateDocument(String authorisation, CaseData caseData, String documentName) throws Exception {
        return toGeneratedDocumentInfo(documentGenService.generateSingleDocument(authorisation, caseData,
                                                                                 documentName, welshCase(caseData)
        ));
    }

    public BulkPrintDetails sendBulkPrint(CaseData caseData, String authorisation,
                                          List<Document> docs, Address address, String name, String servedParty) {
        List<Document> sentDocs = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM YYYY HH:mm:ss");
        LocalDateTime datetime = LocalDateTime.now();
        String currentDate = datetime.format(formatter);
        String bulkPrintedId = "";
        try {
            log.info("*** Initiating request to Bulk print service ***");
            log.info("*** number of files in the pack *** {}", null != docs ? docs.size() : "empty");
            //log.info("*** Documents before calling Bulk Print Service:" + docs);
            log.info("*** calling Bulk Print ***");
            UUID bulkPrintId = bulkPrintService.send(
                String.valueOf(caseData.getId()),
                authorisation,
                LETTER_TYPE,
                docs
            );
            log.info("ID in the queue from bulk print service : {}", bulkPrintId);
            bulkPrintedId = String.valueOf(bulkPrintId);
            sentDocs.addAll(docs);

        } catch (Exception e) {
            log.info("The bulk print service has failed: {}", e);
        }
        return BulkPrintDetails.builder()
            .bulkPrintId(bulkPrintedId)
            .servedParty(servedParty)
            .printedDocs(String.join(",", docs.stream().map(a -> a.getDocumentFileName()).collect(
                Collectors.toList())))
            .recipientsName(name)
            .printDocs(docs.stream().map(e -> element(e)).collect(Collectors.toList()))
            .postalAddress(address)
            .timeStamp(currentDate).build();
    }

}
