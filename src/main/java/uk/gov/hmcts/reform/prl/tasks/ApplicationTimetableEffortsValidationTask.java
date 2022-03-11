package uk.gov.hmcts.reform.prl.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.framework.context.TaskContext;
import uk.gov.hmcts.reform.prl.framework.exceptions.TaskException;
import uk.gov.hmcts.reform.prl.framework.task.Task;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;

import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICATION_NOTICE_EFFORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DAYS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HOURS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_APPLICATION_URGENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;

@Component
public class ApplicationTimetableEffortsValidationTask implements Task<WorkflowResult> {

    public static final String ERROR_MSG_NOTICE_EFFORTS_REQUIRED =
        "Please specify what efforts have been made to put each respondent on notice of the application";

    @Override
    public WorkflowResult execute(TaskContext context, WorkflowResult workflowResult) throws TaskException {
        Map<String, Object> caseData = workflowResult.getCaseData();
        boolean noticeEffortsIsBlank = Objects.toString(caseData.get(APPLICATION_NOTICE_EFFORTS), "").isBlank();

        if (applicationIsUrgent(caseData) && applicationToBeConsideredInLessThan48Hours(caseData) && noticeEffortsIsBlank) {
            workflowResult.getErrors().add(ERROR_MSG_NOTICE_EFFORTS_REQUIRED);
        }

        return workflowResult;
    }

    private boolean applicationIsUrgent(Map<String, Object> caseData) {
        return YES.equals(caseData.get(IS_APPLICATION_URGENT));
    }

    private boolean applicationToBeConsideredInLessThan48Hours(Map<String, Object> caseData) {
        Map<String, Object> applicationConsideredInDaysAndHours = (Map<String, Object>) caseData.get(APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS);
        if (applicationConsideredInDaysAndHours != null) {
            int days = Integer.parseInt(Objects.toString(applicationConsideredInDaysAndHours.get(DAYS), "0"));
            int hours = Integer.parseInt(Objects.toString(applicationConsideredInDaysAndHours.get(HOURS), "0"));

            return days * 24 + hours < 48;
        }
        return false;
    }
}
