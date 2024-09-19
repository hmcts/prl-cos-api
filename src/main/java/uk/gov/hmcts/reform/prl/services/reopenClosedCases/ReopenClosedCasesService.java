package uk.gov.hmcts.reform.prl.services.reopenClosedCases;

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
import uk.gov.hmcts.reform.prl.enums.reopenClosedCases.ValidReopenClosedCasesStatusEnum;
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
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.IncrementalInteger;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_CLOSED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILDREN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINAL_CASE_CLOSED_DATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NEW_CHILDREN;
import static uk.gov.hmcts.reform.prl.services.closingcase.ClosingCaseService.APPLICANT_CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReopenClosedCasesService {

    private final ObjectMapper objectMapper;

    private final ApplicationsTabService applicationsTabService;

    private final ApplicationsTabServiceHelper applicationsTabServiceHelper;

    private final CaseSummaryTabService caseSummaryTab;


    public Map<String, Object> reopenClosedCases(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        removeClosingDetailsFromChildren(caseDataUpdated, caseData);
        reopenTheCase(caseDataUpdated, caseData);
        updateChildDetailsInTab(caseDataUpdated, caseData);
        cleanUpClosingCaseChildOptions(caseDataUpdated);
        return caseDataUpdated;
    }

    private static void removeClosingDetailsFromChildren(Map<String, Object> caseDataUpdated,
                                           CaseData caseData) {
        if ((PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
            || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) && caseData.getNewChildDetails() != null) {
            List<Element<ChildDetailsRevised>> children = caseData.getNewChildDetails();
            caseData.getNewChildDetails().forEach(child -> {
                    ChildDetailsRevised updatedChildDetails = child.getValue().toBuilder()
                        .finalDecisionResolutionDate(null)
                        .finalDecisionResolutionReason(null)
                        .build();
                    children.set(children.indexOf(child), element(child.getId(), updatedChildDetails));
            });
            caseDataUpdated.put(NEW_CHILDREN, children);
        } else if (caseData.getChildren() != null) {
            List<Element<Child>> children = caseData.getChildren();
            caseData.getChildren().forEach(child -> {
                    Child updatedChildDetails = child.getValue().toBuilder()
                        .finalDecisionResolutionDate(null)
                        .finalDecisionResolutionReason(null)
                        .build();
                    children.set(children.indexOf(child), element(child.getId(), updatedChildDetails));
            });
            caseDataUpdated.put(CHILDREN, children);
        } else if (caseData.getApplicantChildDetails() != null) {
            List<Element<ApplicantChild>> children = caseData.getApplicantChildDetails();
            caseData.getApplicantChildDetails().forEach(child -> {
                    ApplicantChild updatedChildDetails = child.getValue().toBuilder()
                        .finalDecisionResolutionDate(null)
                        .finalDecisionResolutionReason(null)
                        .build();
                    children.set(children.indexOf(child), element(child.getId(), updatedChildDetails));
            });
            caseDataUpdated.put(APPLICANT_CHILD_DETAILS, children);
        }
    }

    private void reopenTheCase(Map<String, Object> caseDataUpdated, CaseData caseData) {
        caseDataUpdated.put(FINAL_CASE_CLOSED_DATE, null);
        caseDataUpdated.put(CASE_CLOSED, null);
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        caseDataUpdated.put("reopenState", caseData.getChangeStatusOptions().toString());
        caseData = caseData.toBuilder()
            .finalCaseClosedDate(null)
            .state(ValidReopenClosedCasesStatusEnum.CASE_ISSUED.equals(caseData.getChangeStatusOptions())
                   ? State.CASE_ISSUED : State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
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
}
