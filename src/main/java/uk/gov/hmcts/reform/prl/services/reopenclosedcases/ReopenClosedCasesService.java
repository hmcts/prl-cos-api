package uk.gov.hmcts.reform.prl.services.reopenclosedcases;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.reopenclosedcases.ValidReopenClosedCasesStatusEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.closingcase.ClosingCaseService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_CLOSED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILDREN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINAL_CASE_CLOSED_DATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NEW_CHILDREN;
import static uk.gov.hmcts.reform.prl.services.closingcase.ClosingCaseService.APPLICANT_CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReopenClosedCasesService {

    public static final String REOPEN_STATE_TO = "reopenStateTo";
    private final ObjectMapper objectMapper;
    private final ClosingCaseService closingCaseService;
    private final CaseSummaryTabService caseSummaryTab;


    public Map<String, Object> reopenClosedCases(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        removeClosingDetailsFromChildren(caseDataUpdated, caseData);
        reopenCase(caseDataUpdated, caseData);
        closingCaseService.updateChildDetailsInTab(caseDataUpdated, caseData);
        cleanUpReopenClosedCaseOptions(caseDataUpdated);
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

    private void reopenCase(Map<String, Object> caseDataUpdated, CaseData caseData) {
        caseDataUpdated.put(FINAL_CASE_CLOSED_DATE, null);
        caseDataUpdated.put(CASE_CLOSED, null);
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        caseDataUpdated.put(REOPEN_STATE_TO, caseData.getChangeStatusOptions().toString());
        caseData = caseData.toBuilder()
            .finalCaseClosedDate(null)
            .state(ValidReopenClosedCasesStatusEnum.CASE_ISSUED.equals(caseData.getChangeStatusOptions())
                       ? State.CASE_ISSUED : State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();
        caseDataUpdated.putAll(caseSummaryTab.updateTab(caseData));
    }

    public static void cleanUpReopenClosedCaseOptions(Map<String, Object> caseDataUpdated) {
        caseDataUpdated.remove("changeStatusOptions");
    }
}
