package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.Opt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.OrdersToServeSA;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.utils.DocumentUtils.toGeneratedDocumentInfo;

@Service
@RequiredArgsConstructor
public class ServiceOfApplicationPostService {

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private OrdersToServeSA orders;

    private static String LETTER_TYPE = "RespondentServiceOfApplication";


    public void send(CaseData caseData, String authorisation) {
        bulkPrintService.send(String.valueOf(caseData.getId()), authorisation, LETTER_TYPE, getListOfDocumentInfo(caseData));
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
        return YesOrNo.Yes.equals(caseData.getAllegationOfHarm().getAllegationsOfHarmChildAbuseYesNo());
    }

    private List<GeneratedDocumentInfo> getListOfDocumentInfo(CaseData caseData) {
        List<GeneratedDocumentInfo> docs = new ArrayList<>();
        getC1aDocument(caseData).ifPresent(docs::add);
        docs.add(getFinalDocument(caseData));
        docs.addAll(getSelectedOrders(caseData));
        return docs;
    }

    private List<GeneratedDocumentInfo> getSelectedOrders(CaseData caseData) {
        List<GeneratedDocumentInfo> docs = new ArrayList<>();

        List<String> orderNames = orders.getSelectedOrders().stream()
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
            case "standardDirectionsOrderOption":
                return "Standard directions order";
            case "blankOrderOrDirectionsOption":
                return "Blank order or directions (C21)";
            case "blankOrderOrDirectionsWithdrawOption":
                return "Blank order or directions (C21) - to withdraw application";
            case "childArrangementSpecificOrderOption":
                return "Child arrangements, specific issue or prohibited steps order (C43)";
            case "parentalResponsibilityOption":
                return "Parental responsibility order (C45A)";
            case "specialGuardianShipOption":
                return "Special guardianship order (C43A)";
            case "noticeOfProceedingsPartiesOption":
                return "Notice of proceedings (C6) (Notice to parties)";
            case "noticeOfProceedingsNonPartiesOption":
                return "Notice of proceedings (C6a) (Notice to non-parties)";
            case "transferOfCaseToAnotherCourtOption":
                return "Transfer of case to another court (C49)";
            case "appointmentOfGuardianOption":
                return "Appointment of a guardian (C47A)";
            case "nonMolestationOption":
                return "Non-molestation order (FL404A)";
            case "occupationOption":
                return "Occupation order (FL404)";
            case "powerOfArrestOption":
                return "Power of arrest (FL406)";
            case "amendDischargedVariedOption":
                return "Amended, discharged or varied order (FL404B)";
            case "generalFormUndertakingOption":
                return "General form of undertaking (N117)";
            case "noticeOfProceedingsEnumOption":
                return "Notice of proceedings (FL402)";
            case "otherUploadAnOrderOption":
                return "Other (upload an order)";
            case "blankOrderEnumOption":
                return "Blank order (FL404B)";
            default:
                break;
        }
        return "";
    }







}
