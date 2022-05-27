package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_HINT;
import static uk.gov.hmcts.reform.prl.utils.DocumentUtils.toGeneratedDocumentInfo;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
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
                    bulkPrintService.send(
                        String.valueOf(caseData.getId()),
                        authorisation,
                        LETTER_TYPE,
                        docs
                    );
                    sentDocs.addAll(docs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        return sentDocs;
    }

    private List<GeneratedDocumentInfo> getListOfDocumentInfo(String auth, CaseData caseData, PartyDetails partyDetails) throws Exception {
        List<GeneratedDocumentInfo> docs = new ArrayList<>();

        docs.add(generateCoverSheet(auth, getRespondentCaseData(partyDetails,caseData)));
        docs.add(getFinalDocument(caseData));
        getC1aDocument(caseData).ifPresent(docs::add);
        docs.addAll(getSelectedOrders(caseData));


        return docs;
    }

    private CaseData getRespondentCaseData(PartyDetails partyDetails, CaseData caseData) {
        CaseData respondentCaseData = CaseData
            .builder()
            .id(caseData.getId())
            .respondents(List.of(element(partyDetails)))
            .build();
        return respondentCaseData;
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
        List<String> orderNames = caseData.getServiceOfApplicationScreen1().getSelectedOrders().stream()
            .map(this::getSelectedOrderTypes)
            .collect(Collectors.toList());

        return caseData.getOrderCollection().stream()
            .map(Element::getValue)
            .filter(i -> orderNames.contains(i.getOrderType()))
            .map(i -> toGeneratedDocumentInfo(i.getOrderDocument()))
            .collect(Collectors.toList());

    }

    private String getSelectedOrderTypes(String selectedOrder) {
        switch (selectedOrder) {
            case "standardDirectionsOrder":
                return "Standard directions order";
            case "blankOrderOrDirections":
                return "Blank order or directions (C21)";
            case "blankOrderOrDirectionsWithdraw":
                return "Blank order or directions (C21) - to withdraw application";
            case "childArrangementSpecificOrder":
                return "Child arrangements, specific issue or prohibited steps order (C43)";
            case "parentalResponsibility":
                return "Parental responsibility order (C45A)";
            case "specialGuardianShip":
                return "Special guardianship order (C43A)";
            case "noticeOfProceedingsParties":
                return "Notice of proceedings (C6) (Notice to parties)";
            case "noticeOfProceedingsNonParties":
                return "Notice of proceedings (C6a) (Notice to non-parties)";
            case "transferOfCaseToAnotherCourt":
                return "Transfer of case to another court (C49)";
            case "appointmentOfGuardian":
                return "Appointment of a guardian (C47A)";
            case "nonMolestation":
                return "Non-molestation order (FL404A)";
            case "occupation":
                return "Occupation order (FL404)";
            case "powerOfArrest":
                return "Power of arrest (FL406)";
            case "amendDischargedVaried":
                return "Amended, discharged or varied order (FL404B)";
            case "generalFormUndertaking":
                return "General form of undertaking (N117)";
            case "noticeOfProceedingsEnum":
                return "Notice of proceedings (FL402)";
            case "otherUploadAnOrder":
                return "Other (upload an order)";
            case "blankOrderEnum":
                return "Blank order (FL404B)";
            default:
                break;
        }
        return "";
    }

    private GeneratedDocumentInfo generateCoverSheet(String authorisation, CaseData caseData) throws Exception {
        Document d = documentGenService.generateSingleDocument(authorisation, caseData,
                                                               DOCUMENT_COVER_SHEET_HINT, welshCase(caseData));

        return toGeneratedDocumentInfo(documentGenService.generateSingleDocument(authorisation, caseData,
                                                                      DOCUMENT_COVER_SHEET_HINT, welshCase(caseData)));
    }
}
