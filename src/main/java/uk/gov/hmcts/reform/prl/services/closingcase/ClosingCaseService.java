package uk.gov.hmcts.reform.prl.services.closingcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ClosingCaseFieldsEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.closingcase.CaseClosingReasonForChildren;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabServiceHelper;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.IncrementalInteger;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_CLOSED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILDREN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINAL_CASE_CLOSED_DATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NEW_CHILDREN;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClosingCaseService {

    public static final String CHILD_OPTIONS_FOR_FINAL_DECISION = "childOptionsForFinalDecision";
    public static final String FINAL_OUTCOME_FOR_CHILDREN = "finalOutcomeForChildren";
    public static final String APPLICANT_CHILD_DETAILS = "applicantChildDetails";
    public static final String NO_OF_CHILDREN_RESOLUTION_MADE = "noOfChildrenResolutionMade";

    private final ObjectMapper objectMapper;

    private final ApplicationsTabService applicationsTabService;

    private final ApplicationsTabServiceHelper applicationsTabServiceHelper;

    private final CaseSummaryTabService caseSummaryTab;


    public Map<String, Object> prePopulateChildData(CallbackRequest callbackRequest) {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        caseDataUpdated.put(CHILD_OPTIONS_FOR_FINAL_DECISION, DynamicMultiSelectList.builder()
            .listItems(getChildrenMultiSelectListForFinalDecisions(caseData)).build());
        return caseDataUpdated;
    }

    public List<DynamicMultiselectListElement> getChildrenMultiSelectListForFinalDecisions(CaseData caseData) {
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        if ((PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
            || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) && caseData.getNewChildDetails() != null) {
            IncrementalInteger i = new IncrementalInteger(1);
            caseData.getNewChildDetails().forEach(child -> {
                if (StringUtils.isEmpty(child.getValue().getFinalDecisionResolutionReason())) {
                    listItems.add(DynamicMultiselectListElement.builder().code(child.getId().toString())
                                      .label(child.getValue().getFirstName() + " "
                                                 + child.getValue().getLastName()
                                                 + " (Child " + i.getAndIncrement() + ")").build());
                }
            });

        } else if (caseData.getChildren() != null) {
            IncrementalInteger i = new IncrementalInteger(1);
            caseData.getChildren().forEach(child -> {
                if (StringUtils.isEmpty(child.getValue().getFinalDecisionResolutionReason())) {
                    listItems.add(DynamicMultiselectListElement.builder().code(child.getId().toString())
                                      .label(child.getValue().getFirstName() + " "
                                                 + child.getValue().getLastName()
                                                 + " (Child " + i.getAndIncrement() + ")").build());
                }
            });
        } else if (caseData.getApplicantChildDetails() != null) {
            caseData.getApplicantChildDetails().forEach(child -> {
                if (StringUtils.isEmpty(child.getValue().getFinalDecisionResolutionReason())) {
                    listItems.add(DynamicMultiselectListElement.builder()
                                      .code(child.getId().toString())
                                      .label(child.getValue().getFullName()).build());
                }
            });
        }
        return listItems;
    }

    public Map<String, Object> populateSelectedChildWithFinalOutcome(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren = new ArrayList<>();
        if (YesOrNo.No.equals(caseData.getClosingCaseOptions().getIsTheDecisionAboutAllChildren())) {
            DynamicMultiSelectList childOptionsForFinalDecision = caseData.getClosingCaseOptions().getChildOptionsForFinalDecision();
            childOptionsForFinalDecision.getValue().forEach(dynamicMultiselectListElement ->
                                                                populateFinalOutcomeForChildren(
                                                                    caseData,
                                                                    finalOutcomeForChildren,
                                                                    dynamicMultiselectListElement
                                                                ));
        } else {
            populateFinalOutcomeForChildren(caseData, finalOutcomeForChildren, null);
        }
        caseDataUpdated.put(FINAL_OUTCOME_FOR_CHILDREN, finalOutcomeForChildren);
        caseDataUpdated.put(NO_OF_CHILDREN_RESOLUTION_MADE, Integer.toString(finalOutcomeForChildren.size()));
        return caseDataUpdated;
    }

    public List<String> validateChildDetails(CallbackRequest callbackRequest) {
        List<String> errorList = new ArrayList<>();
        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        if (Integer.valueOf(caseData.getClosingCaseOptions().getNoOfChildrenResolutionMade())
            != caseData.getClosingCaseOptions().getFinalOutcomeForChildren().size()) {
            errorList.add("Children details are altered");
        }
        return errorList;
    }

    public Map<String, Object> closingCaseForChildren(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        String finalDecisionResolutionDate = ObjectUtils.isNotEmpty(
            caseData.getClosingCaseOptions().getDateFinalDecisionWasMade().getFinalDecisionDate())
            ? caseData.getClosingCaseOptions().getDateFinalDecisionWasMade().getFinalDecisionDate().format(
            formatter) : EMPTY_STRING;

        List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren =
            caseData.getClosingCaseOptions().getFinalOutcomeForChildren();
        log.info("finalOutcomeForChildren " + finalOutcomeForChildren);
        finalOutcomeForChildren.forEach(finalOutcomeForChildrenElement ->
                                            updateChildDetails(
                                                caseDataUpdated,
                                                caseData,
                                                finalDecisionResolutionDate,
                                                finalOutcomeForChildrenElement
                                            ));
        if (YesOrNo.Yes.equals(caseData.getClosingCaseOptions().getIsTheDecisionAboutAllChildren())
            || getChildrenMultiSelectListForFinalDecisions(caseData).isEmpty()) {
            markTheCaseAsClosed(caseDataUpdated, finalDecisionResolutionDate, caseData);
        }
        updateChildDetailsInTab(caseDataUpdated, caseData);
        cleanUpClosingCaseChildOptions(caseDataUpdated);
        return caseDataUpdated;
    }

    private static void updateChildDetails(Map<String, Object> caseDataUpdated,
                                           CaseData caseData, String finalDecisionResolutionDate,
                                           Element<CaseClosingReasonForChildren> finalOutcomeForChildrenElement) {
        if ((PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
            || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) && caseData.getNewChildDetails() != null) {
            List<Element<ChildDetailsRevised>> children = caseData.getNewChildDetails();
            caseData.getNewChildDetails().forEach(child -> {
                if (finalOutcomeForChildrenElement.getId().equals(child.getId())) {
                    log.info("found ChildDetailsRevised");
                    ChildDetailsRevised updatedChildDetails = child.getValue().toBuilder()
                        .finalDecisionResolutionDate(finalDecisionResolutionDate)
                        .finalDecisionResolutionReason(finalOutcomeForChildrenElement.getValue().getCaseClosingReason().getDisplayedValue())
                        .build();
                    children.set(children.indexOf(child), element(child.getId(), updatedChildDetails));
                }
            });
            log.info("children ==> " + children);
            log.info("caseData.getNewChildDetails() ==> " + caseData.getNewChildDetails());
            caseDataUpdated.put(NEW_CHILDREN, children);
        } else if (caseData.getChildren() != null) {
            List<Element<Child>> children = caseData.getChildren();
            caseData.getChildren().forEach(child -> {
                if (finalOutcomeForChildrenElement.getId().equals(child.getId())) {
                    log.info("found Child");
                    Child updatedChildDetails = child.getValue().toBuilder()
                        .finalDecisionResolutionDate(finalDecisionResolutionDate)
                        .finalDecisionResolutionReason(finalOutcomeForChildrenElement.getValue().getCaseClosingReason().getDisplayedValue())
                        .build();
                    children.set(children.indexOf(child), element(child.getId(), updatedChildDetails));
                }
            });
            log.info("children ==> " + children);
            log.info("caseData.getChildren() ==> " + caseData.getChildren());
            caseDataUpdated.put(CHILDREN, children);
        } else if (caseData.getApplicantChildDetails() != null) {
            List<Element<ApplicantChild>> children = caseData.getApplicantChildDetails();
            caseData.getApplicantChildDetails().forEach(child -> {
                if (finalOutcomeForChildrenElement.getId().equals(child.getId())) {
                    log.info("found ApplicantChild");
                    ApplicantChild updatedChildDetails = child.getValue().toBuilder()
                        .finalDecisionResolutionDate(finalDecisionResolutionDate)
                        .finalDecisionResolutionReason(finalOutcomeForChildrenElement.getValue().getCaseClosingReason().getDisplayedValue())
                        .build();
                    children.set(children.indexOf(child), element(child.getId(), updatedChildDetails));
                }
            });
            log.info("children ==> " + children);
            log.info("caseData.getApplicantChildDetails() ==> " + caseData.getApplicantChildDetails());
            caseDataUpdated.put(APPLICANT_CHILD_DETAILS, children);
        }
    }

    private void markTheCaseAsClosed(Map<String, Object> caseDataUpdated, String finalDecisionResolutionDate, CaseData caseData) {
        caseDataUpdated.put(FINAL_CASE_CLOSED_DATE, finalDecisionResolutionDate);
        caseDataUpdated.put(CASE_CLOSED, YesOrNo.Yes);
        caseData = caseData.toBuilder()
            .finalCaseClosedDate(finalDecisionResolutionDate)
            .state(State.ALL_FINAL_ORDERS_ISSUED)
            .build();
        caseDataUpdated.putAll(caseSummaryTab.updateTab(caseData));
    }

    public static void cleanUpClosingCaseChildOptions(Map<String, Object> caseDataUpdated) {
        for (ClosingCaseFieldsEnum field : ClosingCaseFieldsEnum.values()) {
            caseDataUpdated.remove(field.getValue());
        }
    }

    private void updateChildDetailsInTab(Map<String, Object> caseDataUpdated, CaseData caseData) {
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            if (PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
                || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) {
                caseDataUpdated.put(
                    "childDetailsRevisedTable",
                    applicationsTabServiceHelper.getChildRevisedDetails(caseData)
                );
            } else {
                caseDataUpdated.put("childDetailsTable", applicationsTabService.getChildDetails(caseData));
            }
        } else {
            Map<String, Object> applicantFamilyMap = applicationsTabService.getApplicantsFamilyDetails(caseData);
            caseDataUpdated.put("applicantFamilyTable", applicantFamilyMap);
            if (("Yes").equals(applicantFamilyMap.get("doesApplicantHaveChildren"))) {
                caseDataUpdated.put("fl401ChildDetailsTable", applicantFamilyMap.get("applicantChild"));
            }
        }
    }

    private void populateFinalOutcomeForChildren(CaseData caseData,
                                                 List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren,
                                                 DynamicMultiselectListElement dynamicMultiselectListElement) {
        if ((PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
            || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) && caseData.getNewChildDetails() != null) {
            caseData.getNewChildDetails().forEach(child -> getChildList(
                finalOutcomeForChildren,
                dynamicMultiselectListElement,
                child.getId(),
                child.getValue().getFirstName() + EMPTY_SPACE_STRING + child.getValue().getLastName(),
                child.getValue().getFinalDecisionResolutionReason()
            ));
        } else if (caseData.getChildren() != null) {
            caseData.getChildren().forEach(child -> getChildList(
                finalOutcomeForChildren,
                dynamicMultiselectListElement,
                child.getId(),
                child.getValue().getFirstName() + EMPTY_SPACE_STRING + child.getValue().getLastName(),
                child.getValue().getFinalDecisionResolutionReason()
            ));
        } else if (caseData.getApplicantChildDetails() != null) {
            caseData.getApplicantChildDetails().forEach(child -> getChildList(
                finalOutcomeForChildren,
                dynamicMultiselectListElement,
                child.getId(),
                child.getValue().getFullName(),
                child.getValue().getFinalDecisionResolutionReason()
            ));
        }
    }

    private void getChildList(List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren,
                                   DynamicMultiselectListElement dynamicMultiselectListElement,
                                   UUID childId, String childName,
                                   String finalDecisionResolutionReason) {
        if ((ObjectUtils.isEmpty(dynamicMultiselectListElement)
            && StringUtils.isEmpty(finalDecisionResolutionReason))
            || (ObjectUtils.isNotEmpty(dynamicMultiselectListElement)
            && dynamicMultiselectListElement.getCode().equals(childId.toString()))) {
            finalOutcomeForChildren.add(getCaseClosingReasonForChildren(
                childId,
                childName
            ));
        }
    }

    private static Element<CaseClosingReasonForChildren> getCaseClosingReasonForChildren(UUID childId, String childName) {
        CaseClosingReasonForChildren caseClosingReasonForChildren = CaseClosingReasonForChildren.builder()
            .childId(childId.toString())
            .childName(childName)
            .build();
        return element(childId, caseClosingReasonForChildren);
    }
}
