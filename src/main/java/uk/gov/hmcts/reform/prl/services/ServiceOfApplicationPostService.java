package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

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
                } catch (Exception e) {
                    log.info("*** Error while generating privacy notice to be served ***");
                }
                CaseData caseDataReturned = sendBulkPrint(caseData, authorisation, docs);
                sentDocs.addAll(caseData.getGeneratedDocs());
            }
            ));
        return sentDocs;
    }

    private List<GeneratedDocumentInfo> getListOfDocumentInfo(String auth, CaseData caseData, PartyDetails partyDetails) throws Exception {
        List<GeneratedDocumentInfo> docs = new ArrayList<>();
        docs.add(generateDocument(auth, getRespondentCaseData(partyDetails,caseData),DOCUMENT_COVER_SHEET_HINT));
        docs.add(getFinalDocument(caseData));
        getC1aDocument(caseData).ifPresent(docs::add);
        docs.addAll(getSelectedOrders(caseData));
        docs.addAll(getUploadedDocumentsServiceOfApplication(caseData));
        CaseData blankCaseData = CaseData.builder().build();
        docs.add(generateDocument(auth, blankCaseData,DOCUMENT_PRIVACY_NOTICE_HINT));
        docs.add(generateDocument(auth, blankCaseData,DOCUMENT_C1A_BLANK_HINT));
        docs.add(generateDocument(auth, blankCaseData,DOCUMENT_C7_DRAFT_HINT));
        docs.add(generateDocument(auth, blankCaseData,DOCUMENT_C8_BLANK_HINT));
        return docs;
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
                                                                      documentName, welshCase(caseData)));
    }

    private CaseData sendBulkPrint(CaseData caseData,String authorisation, List<GeneratedDocumentInfo> docs) {
        List<GeneratedDocumentInfo> sentDocs = new ArrayList<>();
        try {
            log.info("*** Initiating request to Bulk print service ***");
            UUID bulkPrintId = bulkPrintService.send(
                String.valueOf(caseData.getId()),
                authorisation,
                LETTER_TYPE,
                docs
            );
            log.info("ID in the queue from bulk print service : {}",bulkPrintId);
            sentDocs.addAll(docs);
            caseData.setGeneratedDocs(sentDocs);
            caseData.setBulkPrintId(bulkPrintId);
        } catch (Exception e) {
            log.info("The bulk print service has failed: {}", e);
        }
        return caseData;
    }
}
