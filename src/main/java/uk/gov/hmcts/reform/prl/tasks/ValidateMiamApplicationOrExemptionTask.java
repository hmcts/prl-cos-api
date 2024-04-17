package uk.gov.hmcts.reform.prl.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.framework.context.TaskContext;
import uk.gov.hmcts.reform.prl.framework.exceptions.TaskException;
import uk.gov.hmcts.reform.prl.framework.task.Task;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;

import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_ATTENDED_MIAM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CLAIMING_EXEMPTION_MIAM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MPU_APPLICANT_ATTENDED_MIAM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MPU_CHILD_INVOLVED_IN_MIAM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MPU_CLAIMING_EXEMPTION_MIAM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NO;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;

@Component
public class ValidateMiamApplicationOrExemptionTask implements Task<WorkflowResult> {

    public static final String ERROR_MSG_MIAM =
        "You cannot make this application unless the applicant has either attended, or is exempt from attending a MIAM";

    @Override
    public WorkflowResult execute(TaskContext context, WorkflowResult payload) throws TaskException {

        Map<String, Object> caseData = payload.getCaseData();
        if ((TASK_LIST_VERSION_V3.equals(caseData.get("taskListVersion"))
            && NO.equals(caseData.get(MPU_CHILD_INVOLVED_IN_MIAM))
            && NO.equals(caseData.get(MPU_APPLICANT_ATTENDED_MIAM))
            && NO.equals(caseData.get(MPU_CLAIMING_EXEMPTION_MIAM)))
            || (applicantHasNotAttendedMiam(caseData)
            && applicantIsNotClaimingMiamExemption(caseData))) {
            payload.getErrors().add(ERROR_MSG_MIAM);
        }
        return payload;
    }

    private boolean applicantHasNotAttendedMiam(Map<String, Object> caseData) {
        return NO.equals(caseData.get(APPLICANT_ATTENDED_MIAM));
    }

    private boolean applicantIsNotClaimingMiamExemption(Map<String, Object> caseData) {
        return NO.equals(caseData.get(CLAIMING_EXEMPTION_MIAM));
    }
}

