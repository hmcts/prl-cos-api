package uk.gov.hmcts.reform.prl.services.closingcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ClosingCaseFieldsEnum;
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
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClosingCaseService {

    private final ObjectMapper objectMapper;

    private final ApplicationsTabService applicationsTabService;

    private final ApplicationsTabServiceHelper applicationsTabServiceHelper;

    private final DynamicMultiSelectListService dynamicMultiSelectListService;


    public Map<String, Object> prePopulateChildData(CallbackRequest callbackRequest) {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        caseDataUpdated.put("childOptionsForFinalDecision", DynamicMultiSelectList.builder()
            .listItems(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).build());
        return caseDataUpdated;
    }

    public Map<String, Object> populateSelectedChildWithFinalOutcome(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        YesOrNo isTheDecisionAboutAllChildren = caseData.getClosingCaseOptions().getIsTheDecisionAboutAllChildren();
        List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren = new ArrayList<>();
        if (YesOrNo.No.equals(isTheDecisionAboutAllChildren)) {
            DynamicMultiSelectList childOptionsForFinalDecision = caseData.getClosingCaseOptions().getChildOptionsForFinalDecision();
            childOptionsForFinalDecision.getValue().forEach(dynamicMultiselectListElement ->
                populateFinalOutcomeForChildren(caseData, finalOutcomeForChildren, dynamicMultiselectListElement));
        } else {
            populateFinalOutcomeForChildren(caseData, finalOutcomeForChildren, null);
        }
        caseDataUpdated.put("finalOutcomeForChildren", finalOutcomeForChildren);
        return caseDataUpdated;
    }

    public Map<String, Object> closingCaseForChildren(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        String finalDecisionResolutionDate = caseData.getClosingCaseOptions().getDateFinalDecisionWasMade().toString();
        List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren =
            caseData.getClosingCaseOptions().getFinalOutcomeForChildren();
        finalOutcomeForChildren.forEach(finalOutcomeForChildrenElement -> {
            if ((PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
                || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) && caseData.getNewChildDetails() != null) {
                List<Element<ChildDetailsRevised>> children = caseData.getNewChildDetails();
                caseData.getNewChildDetails().forEach(child -> {
                    if (finalOutcomeForChildrenElement.getId().equals(child.getId().toString())) {
                        ChildDetailsRevised updatedChildDetails = child.getValue().toBuilder()
                            .finalDecisionResolutionDate(finalDecisionResolutionDate)
                            .finalDecisionResolutionReason(finalOutcomeForChildrenElement.getValue().getCaseClosingReason().getDisplayedValue())
                            .build();
                        children.set(children.indexOf(child), element(child.getId(), updatedChildDetails));
                    }
                });

            } else if (caseData.getChildren() != null) {
                List<Element<Child>> children = caseData.getChildren();
                caseData.getChildren().forEach(child -> {
                    if (finalOutcomeForChildrenElement.getId().equals(child.getId().toString())) {
                        Child updatedChildDetails = child.getValue().toBuilder()
                            .finalDecisionResolutionDate(finalDecisionResolutionDate)
                            .finalDecisionResolutionReason(finalOutcomeForChildrenElement.getValue().getCaseClosingReason().getDisplayedValue())
                            .build();
                        children.set(children.indexOf(child), element(child.getId(), updatedChildDetails));
                    }
                });
            } else if (caseData.getApplicantChildDetails() != null) {
                List<Element<ApplicantChild>> children = caseData.getApplicantChildDetails();
                caseData.getApplicantChildDetails().forEach(child -> {
                    if (finalOutcomeForChildrenElement.getId().equals(child.getId().toString())) {
                        ApplicantChild updatedChildDetails = child.getValue().toBuilder()
                            .finalDecisionResolutionDate(finalDecisionResolutionDate)
                            .finalDecisionResolutionReason(finalOutcomeForChildrenElement.getValue().getCaseClosingReason().getDisplayedValue())
                            .build();
                        children.set(children.indexOf(child), element(child.getId(), updatedChildDetails));
                    }
                });
            }
        });
        updateChildDetailsInTab(caseDataUpdated, caseData);
        cleanUpClosingCaseChildOptions(caseDataUpdated);
        return caseDataUpdated;
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
            Map<String,Object> applicantFamilyMap = applicationsTabService.getApplicantsFamilyDetails(caseData);
            caseDataUpdated.put("applicantFamilyTable", applicantFamilyMap);
            if (("Yes").equals(applicantFamilyMap.get("doesApplicantHaveChildren"))) {
                caseDataUpdated.put("fl401ChildDetailsTable", applicantFamilyMap.get("applicantChild"));
            }
        }
    }

    private static void populateFinalOutcomeForChildren(CaseData caseData,
                                                        List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren,
                                                        DynamicMultiselectListElement dynamicMultiselectListElement) {
        if ((PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
            || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) && caseData.getNewChildDetails() != null) {
            caseData.getNewChildDetails().forEach(child -> {
                if (ObjectUtils.isEmpty(dynamicMultiselectListElement)
                    || dynamicMultiselectListElement.getCode().equals(child.getId().toString())) {
                    finalOutcomeForChildren.add(getCaseClosingReasonForChildren(
                        child.getId(),
                        child.getValue().getFirstName() + EMPTY_SPACE_STRING + child.getValue().getLastName()
                    ));
                }
            });

        } else if (caseData.getChildren() != null) {
            caseData.getChildren().forEach(child -> {
                if (ObjectUtils.isEmpty(dynamicMultiselectListElement)
                    || dynamicMultiselectListElement.getCode().equals(child.getId().toString())) {
                    finalOutcomeForChildren.add(getCaseClosingReasonForChildren(
                        child.getId(),
                        child.getValue().getFirstName() + EMPTY_SPACE_STRING + child.getValue().getLastName()
                    ));
                }
            });
        } else if (caseData.getApplicantChildDetails() != null) {
            caseData.getApplicantChildDetails().forEach(child -> {
                if (ObjectUtils.isEmpty(dynamicMultiselectListElement)
                    || dynamicMultiselectListElement.getCode().equals(child.getId().toString())) {
                    finalOutcomeForChildren.add(getCaseClosingReasonForChildren(
                        child.getId(),
                        child.getValue().getFullName()
                    ));
                }
            });
        }
    }

    private static Element<CaseClosingReasonForChildren> getCaseClosingReasonForChildren(UUID childId, String childName) {
        CaseClosingReasonForChildren caseClosingReasonForChildren = CaseClosingReasonForChildren.builder()
            .childId(childId.toString())
            .childName(childName)
            .build();
        return element(childId,caseClosingReasonForChildren);
    }

}
