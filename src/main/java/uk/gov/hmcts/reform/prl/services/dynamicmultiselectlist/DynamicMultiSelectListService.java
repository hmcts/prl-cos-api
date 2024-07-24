package uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.IncrementalInteger;

import java.time.LocalDateTime;
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

    public static final String REQUESTED_LR_REMOVAL = "Requested LR removal";

    public DynamicMultiSelectList getOrdersAsDynamicMultiSelectList(CaseData caseData) {

        List<Element<OrderDetails>> orders = caseData.getOrderCollection();
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        if (null != orders) {
            orders.forEach(order -> listItems.add(DynamicMultiselectListElement.builder().code(String.valueOf(order.getId()))
                              .label(order.getValue().getLabelForDynamicList()).build()));
        }
        return DynamicMultiSelectList.builder().listItems(listItems).build();
    }

    public List<DynamicMultiselectListElement> getChildrenMultiSelectList(CaseData caseData) {
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        if ((PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
                || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) && caseData.getNewChildDetails() != null) {
            IncrementalInteger i = new IncrementalInteger(1);
            caseData.getNewChildDetails().forEach(child -> {
                if (!YesOrNo.Yes.equals(child.getValue().getIsFinalOrderIssued())) {
                    listItems.add(DynamicMultiselectListElement.builder().code(child.getId().toString())
                            .label(child.getValue().getFirstName() + " "
                                    + child.getValue().getLastName()
                                    + " (Child " + i.getAndIncrement() + ")").build());
                }
            });

        } else if (caseData.getChildren() != null) {
            IncrementalInteger i = new IncrementalInteger(1);
            caseData.getChildren().forEach(child -> {
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
            String code = caseData.getRespondentsFL401().getPartyId().toString();
            respondentSolicitorList.add(DynamicMultiselectListElement.builder()
                                            .code(code)
                                            .label(caseData.getRespondentsFL401().getRepresentativeFirstName() + " "
                                                       + caseData.getRespondentsFL401().getRepresentativeLastName()
                                                       + " (Respondent solicitor)")
                                            .build());
            listItems.add(DynamicMultiselectListElement.builder()
                              .code(String.valueOf(caseData.getRespondentsFL401().getPartyId()))
                                        .label(name).build());
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
            String code = caseData.getApplicantsFL401().getPartyId().toString();
            applicantSolicitorList.add(DynamicMultiselectListElement.builder().code(code)
                                           .label(caseData.getApplicantsFL401().getFirstName() + " "
                                                      + caseData.getApplicantsFL401().getRepresentativeLastName()
                                                      + "(Applicant solicitor)").build());
            listItems.add(DynamicMultiselectListElement.builder()
                              .code(String.valueOf(caseData.getApplicantsFL401().getPartyId()))
                              .label(name).build());
        }
        Map<String, List<DynamicMultiselectListElement>> applicantdetails = new HashMap<>();
        applicantdetails.put("applicants", listItems);
        applicantdetails.put("applicantSolicitors", applicantSolicitorList);
        return applicantdetails;
    }

    public List<DynamicMultiselectListElement> getOtherPeopleMultiSelectList(CaseData caseData) {
        List<DynamicMultiselectListElement> otherPeopleList = new ArrayList<>();

        if ((PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
                || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion()))
                && caseData.getOtherPartyInTheCaseRevised() != null) {
            caseData.getOtherPartyInTheCaseRevised().forEach(others ->
                    otherPeopleList.add(DynamicMultiselectListElement.builder()
                            .code(others.getId().toString())
                            .label(others.getValue().getFirstName()
                                    + " "
                                    + others.getValue().getLastName())
                            .build())
            );
            return otherPeopleList;
        }

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
                                                          strList.add(value.getLabel().split("\\(")[0].trim())
            );
        }
        if (!strList.isEmpty()) {
            return String.join(", ", strList);
        }
        return "";
    }

    public List<Element<ServedParties>> getServedPartyDetailsFromDynamicSelectList(DynamicMultiSelectList dynamicMultiSelectList) {
        List<Element<ServedParties>> servedParties = new ArrayList<>();
        if (dynamicMultiSelectList != null && dynamicMultiSelectList.getValue() != null) {
            dynamicMultiSelectList.getValue().forEach(value -> servedParties
                .add(Element.<ServedParties>builder().value(ServedParties.builder()
                                                                .partyId(value.getCode())
                                                                .partyName(value.getLabel())
                                                                .servedDateTime(LocalDateTime.now())
                                                                .build()).build())
            );
        }
        return servedParties;
    }

    public String getStringFromDynamicMultiSelectListFromListItems(DynamicMultiSelectList dynamicMultiSelectList) {
        List<String> strList = new ArrayList<>();
        if (null != dynamicMultiSelectList && null != dynamicMultiSelectList.getListItems()) {
            dynamicMultiSelectList.getListItems().forEach(value -> {
                if (null != value.getLabel()) {
                    strList.add(value.getLabel().split("\\(")[0].trim());
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
            && (YesOrNo.No.equals(caseData.getManageOrders().getIsTheOrderAboutAllChildren()
        ) || YesOrNo.Yes.equals(caseData.getManageOrders().getIsTheOrderAboutChildren()))
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
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {

            if (PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
                    || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) {

                Optional<ChildDetailsRevised> childRevised = caseData.getNewChildDetails().stream()
                        .filter(element -> element.getId().toString().equalsIgnoreCase(id))
                        .map(Element::getValue)
                        .findFirst();
                return childRevised.map(childDetailsRevised -> Child.builder().firstName(childDetailsRevised.getFirstName())
                        .lastName(childDetailsRevised.getLastName())
                        .dateOfBirth(childDetailsRevised.getDateOfBirth())
                        .gender(childDetailsRevised.getGender()).build()).orElse(null);
            }
            return caseData.getChildren().stream().filter(element -> element.getId()
                            .toString().equalsIgnoreCase(id))
                    .map(Element::getValue)
                    .findFirst().orElseGet(() -> null);

        }
        return null;
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
            getRemoveLegalRepAndPartiesListForCa(caseData, listItems);
        } else {
            getRemoveLegalRepAndPartiesListForDa(caseData, listItems);
        }
        return DynamicMultiSelectList.builder().listItems(listItems).build();
    }

    private static void getRemoveLegalRepAndPartiesListForDa(CaseData caseData, List<DynamicMultiselectListElement> listItems) {
        if (YesOrNo.Yes.equals(caseData.getApplicantsFL401().getUser().getSolicitorRepresented())
            || YesNoDontKnow.yes.equals(caseData.getApplicantsFL401().getDoTheyHaveLegalRepresentation())
            || (caseData.getApplicantsFL401().getSolicitorOrg() != null
            && caseData.getApplicantsFL401().getSolicitorOrg().getOrganisationID() != null)) {
            addSolicitorRepresentedParties(
                listItems,
                caseData.getApplicantsFL401().getPartyId(),
                caseData.getApplicantsFL401()
            );
        }
        if (YesOrNo.Yes.equals(caseData.getRespondentsFL401().getUser().getSolicitorRepresented())) {
            addSolicitorRepresentedParties(
                listItems,
                caseData.getRespondentsFL401().getPartyId(),
                caseData.getRespondentsFL401()
            );
        }
    }

    private static void getRemoveLegalRepAndPartiesListForCa(CaseData caseData, List<DynamicMultiselectListElement> listItems) {
        caseData.getApplicants().stream().forEach(applicant -> {
            PartyDetails partyDetails = applicant.getValue();
            if (YesOrNo.Yes.equals(partyDetails.getUser().getSolicitorRepresented())
                || YesNoDontKnow.yes.equals(partyDetails.getDoTheyHaveLegalRepresentation())
                || (partyDetails.getSolicitorOrg() != null && partyDetails.getSolicitorOrg().getOrganisationID() != null)) {
                addSolicitorRepresentedParties(listItems, applicant.getId(), partyDetails);
            }
        });
        caseData.getRespondents().stream().forEach(respondent -> {
            PartyDetails partyDetails = respondent.getValue();
            if (YesOrNo.Yes.equals(partyDetails.getUser().getSolicitorRepresented())) {
                addSolicitorRepresentedParties(listItems, respondent.getId(), partyDetails
                );
            }
        });
    }

    private static void addSolicitorRepresentedParties(List<DynamicMultiselectListElement> listItems, UUID id,
                                                       PartyDetails partyDetails) {
        StringBuilder label = new StringBuilder();
        label.append(partyDetails.getRepresentativeFirstName()).append(EMPTY_SPACE_STRING)
            .append(partyDetails.getRepresentativeLastName()).append(EMPTY_SPACE_STRING).append("(")
            .append(partyDetails.getFirstName()).append(EMPTY_SPACE_STRING).append(partyDetails.getLastName())
            .append(")");

        if (YesOrNo.Yes.equals(partyDetails.getIsRemoveLegalRepresentativeRequested())) {
            label.append(EMPTY_SPACE_STRING).append("-").append(EMPTY_SPACE_STRING).append(REQUESTED_LR_REMOVAL);
        }
        listItems.add(DynamicMultiselectListElement
                          .builder()
                          .code(String.valueOf(id))
                          .label(label.toString())
                          .build());
    }

    public DynamicMultiSelectList getEmptyDynMultiSelectList() {
        return  DynamicMultiSelectList.builder()
            .listItems(List.of(DynamicMultiselectListElement.EMPTY))
            .value(List.of(DynamicMultiselectListElement.EMPTY))
            .build();
    }
}
