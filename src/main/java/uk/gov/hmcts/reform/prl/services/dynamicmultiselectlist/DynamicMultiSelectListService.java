package uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.IncrementalInteger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor
public class DynamicMultiSelectListService {

    private final UserService userService;

    public DynamicMultiSelectList getOrdersAsDynamicMultiSelectList(CaseData caseData, String key) {
        List<Element<OrderDetails>> orders = caseData.getOrderCollection();
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        if (null != orders) {
            orders.forEach(order -> {
                OrderDetails orderDetails = order.getValue();
                if (ManageOrdersOptionsEnum.servedSavedOrders.getDisplayedValue().equals(key)
                    && orderDetails.getOtherDetails() != null
                    && orderDetails.getOtherDetails().getOrderServedDate() != null) {
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
        IncrementalInteger i = new IncrementalInteger(1);
        if (children != null) {
            children.forEach(child -> {
                if (!YesOrNo.Yes.equals(child.getValue().getIsFinalOrderIssued())) {
                    listItems.add(DynamicMultiselectListElement.builder().code(child.getId().toString())
                                      .label(child.getValue().getFirstName() + " "
                                                 + child.getValue().getLastName()
                                                 + " (Child " + i.getAndIncrement() + ")").build());
                }
            });
        } else if (caseData.getApplicantChildDetails() != null) {
            caseData.getApplicantChildDetails().forEach(child -> listItems.add(DynamicMultiselectListElement.builder()
                                                                                   .code(child.getId().toString())
                                                                                   .label(child.getValue().getFullName()).build()));
        }
        return listItems;
    }

    public Map<String, List<DynamicMultiselectListElement>> getRespondentsMultiSelectList(CaseData caseData) {
        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        List<DynamicMultiselectListElement> respondentSolicitorList = new ArrayList<>();
        IncrementalInteger i = new IncrementalInteger(1);
        IncrementalInteger j = new IncrementalInteger(1);
        if (respondents != null) {
            respondents.forEach(respondent -> {
                listItems.add(DynamicMultiselectListElement.builder().code(respondent.getId().toString())
                                  .label(respondent.getValue().getFirstName() + " "
                                             + respondent.getValue().getLastName()
                                             + " (Respondent " + i.getAndIncrement() + ")").build());
                if (YesNoDontKnow.yes.equals(respondent.getValue().getDoTheyHaveLegalRepresentation())) {
                    respondentSolicitorList.add(DynamicMultiselectListElement.builder()
                                                    .code(respondent.getId().toString())
                                                    .label(respondent.getValue().getRepresentativeFirstName() + " "
                                                               + respondent.getValue().getRepresentativeLastName()
                                                               + " (Respondent solicitor " + j.getAndIncrement() + ")")
                                                    .build());
                }
            });
        } else if (caseData.getRespondentsFL401() != null) {
            String name = caseData.getRespondentsFL401().getFirstName() + " "
                + caseData.getRespondentsFL401().getLastName()
                + " (Respondent)";
            respondentSolicitorList.add(DynamicMultiselectListElement.builder()
                                            .code(name)
                                            .label(caseData.getRespondentsFL401().getRepresentativeFirstName() + " "
                                                       + caseData.getRespondentsFL401().getRepresentativeLastName()
                                                       + " (Respondent solicitor)")
                                            .build());
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
        IncrementalInteger i = new IncrementalInteger(1);
        IncrementalInteger j = new IncrementalInteger(1);
        if (applicants != null) {
            applicants.forEach(applicant -> {
                listItems.add(DynamicMultiselectListElement.builder().code(applicant.getId().toString())
                                  .label(applicant.getValue().getFirstName() + " "
                                             + applicant.getValue().getLastName()
                                             + " (Applicant " + i.getAndIncrement() + ")").build());
                applicantSolicitorList.add(DynamicMultiselectListElement.builder()
                                               .code(applicant.getId().toString())
                                               .label(applicant.getValue().getRepresentativeFirstName() + " "
                                                          + applicant.getValue().getRepresentativeLastName()
                                                          + " (Applicant Solicitor " + j.getAndIncrement() + ")")
                                               .build());
            });
        } else if (caseData.getApplicantsFL401() != null) {
            String name = caseData.getApplicantsFL401().getFirstName() + " "
                + caseData.getApplicantsFL401().getLastName()
                + "(Applicant)";
            applicantSolicitorList.add(DynamicMultiselectListElement.builder().code(name)
                                           .label(caseData.getApplicantsFL401().getFirstName() + " "
                                                      + caseData.getApplicantsFL401().getRepresentativeLastName()
                                                      + "(Applicant solicitor)").build());
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

    public String getStringFromDynamicMultiSelectList(DynamicMultiSelectList dynamicMultiSelectList) {
        List<String> strList = new ArrayList<>();
        if (null != dynamicMultiSelectList && null != dynamicMultiSelectList.getValue()) {
            dynamicMultiSelectList.getValue().forEach(value ->
                                                          strList.add(value.getLabel().split("\\(")[0])
            );
        }
        if (!strList.isEmpty()) {
            return String.join(", ", strList);
        }
        return "";
    }

    public String getStringFromDynamicMultiSelectListFromListItems(DynamicMultiSelectList dynamicMultiSelectList) {
        List<String> strList = new ArrayList<>();
        if (null != dynamicMultiSelectList && null != dynamicMultiSelectList.getListItems()) {
            dynamicMultiSelectList.getListItems().forEach(value -> {
                if (null != value.getLabel()) {
                    strList.add(value.getLabel().split("\\(")[0]);
                }
            });
        }
        if (!strList.isEmpty()) {
            return String.join(", ", strList);
        }
        return "";
    }

    public List<Element<Child>> getChildrenForDocmosis(CaseData caseData) {
        List<Element<Child>> childList = new ArrayList<>();
        if (null != caseData.getManageOrders()
            && YesOrNo.No.equals(caseData.getManageOrders().getIsTheOrderAboutAllChildren())
            && null != caseData.getManageOrders().getChildOption()
            && null != caseData.getManageOrders().getChildOption().getValue()) {
            caseData.getManageOrders().getChildOption().getValue().forEach(value -> {
                Child child = getChildDetails(caseData, value.getCode());
                if (null != child) {
                    childList.add(element(child));

                }
            });
        }
        return childList;
    }

    public List<Element<ApplicantChild>> getApplicantChildDetailsForDocmosis(CaseData caseData) {
        List<Element<ApplicantChild>> applicantChildList = new ArrayList<>();
        if (null != caseData.getManageOrders()
            && YesOrNo.Yes.equals(caseData.getManageOrders().getIsTheOrderAboutChildren())
            && null != caseData.getManageOrders().getChildOption()
            && null != caseData.getManageOrders().getChildOption().getValue()) {
            caseData.getManageOrders().getChildOption().getValue().forEach(value -> {
                ApplicantChild applicantChild = getApplicantChildDetails(caseData, value.getCode());
                if (null != applicantChild) {
                    applicantChildList.add(element(applicantChild));

                }
            });
        }
        return applicantChildList;
    }

    private Child getChildDetails(CaseData caseData, String id) {
        Optional<Child> child = Optional.empty();
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            child = caseData.getChildren().stream().filter(element -> element.getId().toString().equalsIgnoreCase(id))
                .map(Element::getValue)
                .findFirst();
        }
        return child.orElseGet(() -> null);
    }

    private ApplicantChild getApplicantChildDetails(CaseData caseData, String id) {
        Optional<ApplicantChild> applicantChild = Optional.empty();
        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            && null != caseData.getApplicantChildDetails()) {
            applicantChild = caseData.getApplicantChildDetails().stream().filter(element -> element.getId().toString().equalsIgnoreCase(
                id))
                .map(Element::getValue)
                .findFirst();
        }
        return applicantChild.orElseGet(() -> null);
    }

    public DynamicMultiSelectList getSolicitorRepresentedParties(List<Element<PartyDetails>> partyElementList) {
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        partyElementList.stream().forEach(x -> {
            if (x.getId() != null) {
                listItems.add(DynamicMultiselectListElement
                                  .builder()
                                  .code(String.valueOf(x.getId()))
                                  .label(x.getValue().getLabelForDynamicList())
                                  .build());
            } else {
                listItems.add(DynamicMultiselectListElement
                                  .builder()
                                  .code(String.valueOf(x.getValue().getPartyId()))
                                  .label(x.getValue().getLabelForDynamicList())
                                  .build());
            }
        });
        return DynamicMultiSelectList.builder().listItems(listItems).build();
    }

    public DynamicMultiSelectList getRemoveLegalRepAndPartiesList(CaseData caseData) {
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            caseData.getApplicants().stream().forEach(applicant -> {
                PartyDetails partyDetails = applicant.getValue();
                if (YesOrNo.Yes.equals(partyDetails.getUser().getSolicitorRepresented())
                    || YesNoDontKnow.yes.equals(partyDetails.getDoTheyHaveLegalRepresentation())
                    || (partyDetails.getSolicitorOrg() != null && partyDetails.getSolicitorOrg().getOrganisationID() != null)) {
                    addSolicitorRespresentedParties(listItems, applicant.getId(), partyDetails);
                }
            });
            caseData.getRespondents().stream().forEach(respondent -> {
                PartyDetails partyDetails = respondent.getValue();
                if (YesOrNo.Yes.equals(partyDetails.getUser().getSolicitorRepresented())) {
                    addSolicitorRespresentedParties(listItems, respondent.getId(), partyDetails
                    );
                }
            });
        } else {
            if (YesOrNo.Yes.equals(caseData.getApplicantsFL401().getUser().getSolicitorRepresented())
                || YesNoDontKnow.yes.equals(caseData.getApplicantsFL401().getDoTheyHaveLegalRepresentation())
                || (caseData.getApplicantsFL401().getSolicitorOrg() != null
                && caseData.getApplicantsFL401().getSolicitorOrg().getOrganisationID() != null)) {
                addSolicitorRespresentedParties(
                    listItems,
                    caseData.getApplicantsFL401().getPartyId(),
                    caseData.getApplicantsFL401()
                );
            }
            if (YesOrNo.Yes.equals(caseData.getRespondentsFL401().getUser().getSolicitorRepresented())) {
                addSolicitorRespresentedParties(listItems,
                                                caseData.getRespondentsFL401().getPartyId(),
                                                caseData.getRespondentsFL401()
                );
            }
        }
        return DynamicMultiSelectList.builder().listItems(listItems).build();
    }

    private static void addSolicitorRespresentedParties(List<DynamicMultiselectListElement> listItems, UUID id,
                                                        PartyDetails partyDetails) {
        listItems.add(DynamicMultiselectListElement
                          .builder()
                          .code(String.valueOf(id))
                          .label(partyDetails.getRepresentativeFirstName()
                                     + EMPTY_SPACE_STRING + partyDetails.getRepresentativeLastName()
                                     + EMPTY_SPACE_STRING + "("
                                     + partyDetails.getFirstName() + EMPTY_SPACE_STRING
                                     + partyDetails.getLastName() + ")")
                          .build());
    }
}
