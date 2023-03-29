package uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DynamicMultiSelectListService {

    public DynamicMultiSelectList getOrdersAsDynamicMultiSelectList(CaseData caseData, String key) {
        List<Element<OrderDetails>> orders = caseData.getOrderCollection();
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        if (null != orders) {
            orders.forEach(order -> {
                OrderDetails orderDetails = order.getValue();
                if (ManageOrdersOptionsEnum.servedSavedOrders.getDisplayedValue().equals(key)
                    && orderDetails.getOtherDetails() != null
                    &&  orderDetails.getOtherDetails().getOrderServedDate() != null) {
                    return;
                }
                listItems.add(DynamicMultiselectListElement.builder().code(orderDetails.getOrderTypeId() + "-"
                                                                               + orderDetails.getDateCreated())
                                  .label(orderDetails.getLabelForDynamicList()).build());
            });
        }
        return DynamicMultiSelectList.builder().listItems(listItems).build();
    }

    public List<DynamicMultiselectListElement> getChildrenMultiSelectList(CaseData caseData) {
        List<Element<Child>> children = caseData.getChildren();
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        if (children != null) {
            children.forEach(child -> {
                if (!YesOrNo.Yes.equals(child.getValue().getIsFinalOrderIssued())) {
                    listItems.add(DynamicMultiselectListElement.builder().code(child.getId().toString())
                                         .label(child.getValue().getFirstName() + " "
                                                    + child.getValue().getLastName()).build());
                }
            });
        } else if (caseData.getApplicantChildDetails() != null) {
            caseData.getApplicantChildDetails().forEach(child -> {
                listItems.add(DynamicMultiselectListElement.builder().code(child.getId().toString())
                                     .label(child.getValue().getFullName()).build());
            });
        }
        return listItems;
    }

    public Map<String, List<DynamicMultiselectListElement>> getRespondentsMultiSelectList(CaseData caseData) {
        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        List<DynamicMultiselectListElement> respondentSolicitorList = new ArrayList<>();
        if (respondents != null) {
            respondents.forEach(respondent -> {
                listItems.add(DynamicMultiselectListElement.builder().code(respondent.getId().toString())
                                  .label(respondent.getValue().getFirstName() + " "
                                             + respondent.getValue().getLastName()).build());
                if (YesNoDontKnow.yes.equals(respondent.getValue().getDoTheyHaveLegalRepresentation())) {
                    respondentSolicitorList.add(DynamicMultiselectListElement.builder()
                                                    .code(respondent.getId().toString())
                                                    .label(respondent.getValue().getRepresentativeFirstName() + " "
                                                               + respondent.getValue().getRepresentativeLastName())
                                                    .build());
                }
            });
        } else if (caseData.getRespondentsFL401() != null) {
            String name = caseData.getRespondentsFL401().getFirstName() + " " + caseData.getRespondentsFL401().getLastName();
            listItems.add(DynamicMultiselectListElement.builder().code(name).label(name).build());
        }
        Map<String, List<DynamicMultiselectListElement>> respondentdetails = new HashMap<>();
        respondentdetails.put("respondents", listItems);
        respondentdetails.put("respondentSolicitors", respondentSolicitorList);
        return respondentdetails;
    }

    public Map<String, List<DynamicMultiselectListElement>> getApplicantsMultiSelectList(CaseData caseData) {
        List<Element<PartyDetails>> applicants = caseData.getApplicants();
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        List<DynamicMultiselectListElement> applicantSolicitorList = new ArrayList<>();

        if (applicants != null) {
            applicants.forEach(applicant -> {
                listItems.add(DynamicMultiselectListElement.builder().code(applicant.getId().toString())
                                  .label(applicant.getValue().getFirstName() + " "
                                             + applicant.getValue().getLastName()).build());
                applicantSolicitorList.add(DynamicMultiselectListElement.builder()
                                               .code(applicant.getId().toString())
                                               .label(applicant.getValue().getRepresentativeFirstName() + " "
                                                          + applicant.getValue().getRepresentativeLastName())
                                               .build());
            });
        } else if (caseData.getApplicantsFL401() != null) {
            String name = caseData.getApplicantsFL401().getFirstName() + " " + caseData.getApplicantsFL401().getLastName();
            listItems.add(DynamicMultiselectListElement.builder().code(name).label(name).build());
        }
        Map<String, List<DynamicMultiselectListElement>> applicantdetails = new HashMap<>();
        applicantdetails.put("applicants", listItems);
        applicantdetails.put("applicantSolicitors", applicantSolicitorList);
        return applicantdetails;
    }

    public List<DynamicMultiselectListElement> getOtherPeopleMultiSelectList(CaseData caseData) {
        List<DynamicMultiselectListElement> otherPeopleList = new ArrayList<>();
        if (caseData.getOthersToNotify() != null) {
            caseData.getOthersToNotify().forEach(others ->
                                                     otherPeopleList.add(DynamicMultiselectListElement.builder()
                                                                             .code(others.getId().toString())
                                                                             .label(others.getValue().getFirstName()
                                                                                        + " "
                                                                                        + others.getValue().getLastName())
                                                                             .build())
            );
        }
        return otherPeopleList;
    }
}
