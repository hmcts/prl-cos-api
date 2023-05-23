package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C1A_BLANK_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C7_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C8_BLANK_HINT;
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

    private static final String LETTER_TYPE = "RespondentServiceOfApplication";

    public List<GeneratedDocumentInfo> send(CaseData caseData, String authorisation) {
        // Sends post to the respondents who are not represented by a solicitor
        List<GeneratedDocumentInfo> sentDocs = new ArrayList<>();
        caseData.getRespondents().stream()
            .map(Element::getValue)
            .filter(partyDetails -> !YesNoDontKnow.yes.equals(partyDetails.getDoTheyHaveLegalRepresentation()))
            .filter(partyDetails -> YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown()))
            .forEach(partyDetails -> {
                try {
                    List<GeneratedDocumentInfo> docs = getListOfDocumentInfo(authorisation, caseData, partyDetails);
                    log.info("*** Initiating request to Bulk print service ***");
                    log.info("*** Documents before calling Bulk Print Service:" + docs);

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

    public CaseData sendDocs(CaseData caseData, String authorisation) {
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
                List<Element<BulkPrintDetails>> printedDocCollectionList;
                docs = getUploadedDocumentsServiceOfApplication(caseData);
                try {
                    docs.add(generateDocument(authorisation, blankCaseData, DOCUMENT_PRIVACY_NOTICE_HINT));
                    docs.add(getCoverLetterGeneratedDocInfo(caseData, authorisation));
                } catch (Exception e) {
                    log.info("*** Error while generating privacy notice to be served ***");
                }
                if (caseData.getBulkPrintDetails() != null) {
                    printedDocCollectionList = caseData.getBulkPrintDetails();
                } else {
                    printedDocCollectionList = new ArrayList<>();
                }
                printedDocCollectionList.add((sendBulkPrint(caseData, authorisation, docs, partyDetails)));
                caseData.setBulkPrintDetails(printedDocCollectionList);
            }
            ));
        return caseData;
    }

    public void sendPostNotificationToParty(CaseData caseData, String authorisation, PartyDetails partyDetails, List<Document> docs) {
        // Sends post
        List<GeneratedDocumentInfo> documents = null;
        List<Element<BulkPrintDetails>> printedDocCollectionList = new ArrayList<>();
        try {
            log.info("*** Generating Document ***");
            documents = getDocsAsGeneratedDocumentInfo(docs);
            log.info("*** Documents generated ***" + documents);
        } catch (Exception e) {
            log.info("*** Error while generating document ***");
        }
        if (caseData.getBulkPrintDetails() != null) {
            log.info("*** BulkPrintdetails object available in case data ***" + caseData.getBulkPrintDetails());
            caseData.getBulkPrintDetails().forEach(printedDocCollectionList::add);
        } else {
            log.info("*** BulkPrintdetails object empty in case data ***");
            printedDocCollectionList = null;
        }
        log.info("*** calling Bulk Print ***");
        printedDocCollectionList.add(sendBulkPrint(caseData, authorisation, documents, partyDetails));
        caseData.setBulkPrintDetails(printedDocCollectionList);
        log.info("*** Bulk Print details updated in case data ***" + caseData.getBulkPrintDetails());
    }


    private List<GeneratedDocumentInfo> getListOfDocumentInfo(String auth, CaseData caseData, PartyDetails partyDetails) throws Exception {
        List<GeneratedDocumentInfo> docs = new ArrayList<>();
        docs.add(generateDocument(auth, getRespondentCaseData(partyDetails, caseData), DOCUMENT_COVER_SHEET_HINT));
        docs.add(getFinalDocument(caseData));
        getC1aDocument(caseData).ifPresent(docs::add);
        docs.addAll(getSelectedOrders(caseData));
        docs.addAll(getUploadedDocumentsServiceOfApplication(caseData));
        CaseData blankCaseData = CaseData.builder().build();
        docs.add(generateDocument(auth, blankCaseData, DOCUMENT_PRIVACY_NOTICE_HINT));
        docs.add(generateDocument(auth, blankCaseData, DOCUMENT_C1A_BLANK_HINT));
        docs.add(generateDocument(auth, blankCaseData, DOCUMENT_C7_DRAFT_HINT));
        docs.add(generateDocument(auth, blankCaseData, DOCUMENT_C8_BLANK_HINT));
        return docs;
    }

    public Document getCoverLetter(String auth, Address address, CaseData caseData) throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo = null;
        generatedDocumentInfo = getCoverLetterGeneratedDocInfo(caseData, auth);
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

    public GeneratedDocumentInfo getCoverLetterGeneratedDocInfo(CaseData caseData, String auth) throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo = null;
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if (documentLanguage.isGenEng()) {
            generatedDocumentInfo = dgsService.generateDocument(
                auth,
                CaseDetails.builder().caseData(caseData.toBuilder()
                                                   .coverPageAddress(Address.builder()
                                                                         .addressLine1("abc").addressLine3("ddd")
                                                                         .county("county").postCode("postcode")
                                                                         .postTown("posttown").build()).build()).build(),
                documentGenService.getTemplate(caseData, DOCUMENT_COVER_SHEET_HINT, false)
            );
        }
        return generatedDocumentInfo;
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

    private GeneratedDocumentInfo getFinalDocument(CaseData caseData) {
        if (!welshCase(caseData)) {
            return toGeneratedDocumentInfo(caseData.getFinalDocument());
        }
        return toGeneratedDocumentInfo(caseData.getFinalWelshDocument());
    }

    private Optional<GeneratedDocumentInfo> getC1aDocument(CaseData caseData) {
        if (hasAllegationsOfHarm(caseData)) {
            if (!welshCase(caseData)) {
                return Optional.of(toGeneratedDocumentInfo(caseData.getC1ADocument()));
            }
            return Optional.of(toGeneratedDocumentInfo(caseData.getC1AWelshDocument()));
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

    public Element<BulkPrintDetails> sendBulkPrint(CaseData caseData, String authorisation,
                                                   List<GeneratedDocumentInfo> docs, PartyDetails partyDetails) {
        List<GeneratedDocumentInfo> sentDocs = new ArrayList<>();
        String bulkPrintedId = "";
        try {
            log.info("*** Initiating request to Bulk print service ***");
            log.info("*** number of files in the pack *** {}", null != docs ? docs.size() : "empty");
            log.info("*** Documents before calling Bulk Print Service:" + docs);
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
        return element(BulkPrintDetails.builder()
                           .bulkPrintId(bulkPrintedId)
                           .printedDocs(sentDocs)
                           .recipientsName(partyDetails.getFirstName() + " " + partyDetails.getLastName())
                           .timeStamp(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now(ZoneId.of(
                               "Europe/London")))).build());
    }

    private List<GeneratedDocumentInfo> getDocsAsGeneratedDocumentInfo(List<Document> docs) {
        List<GeneratedDocumentInfo> documents = new ArrayList<>();
        docs.forEach(doc -> {
            documents.add(toGeneratedDocumentInfo(doc));
        });
        return documents;
    }


}
