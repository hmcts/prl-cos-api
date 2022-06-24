package uk.gov.hmcts.reform.prl.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.framework.context.DefaultTaskContext;
import uk.gov.hmcts.reform.prl.framework.context.TaskContext;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DAYS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HOURS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_APPLICATION_URGENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NO;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;
import static uk.gov.hmcts.reform.prl.tasks.ApplicationTimetableTimeValidationTask.ERROR_MSG_NOTICE_DATE_OR_TIME_REQUIRED;

public class ApplicationTimetableTimeValidationTaskTest {

    private ApplicationTimetableTimeValidationTask applicationTimetableTimeValidationTask
        = new ApplicationTimetableTimeValidationTask();

    @Test
    public void whenNoApplicationToBeConsideredHoursOrDays_thenErrorReturnedInResult() {
        WorkflowResult workflowResult = new WorkflowResult(ImmutableMap.of(
            IS_APPLICATION_URGENT, YES,
            APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS, ImmutableMap.of()));
        TaskContext taskContext = new DefaultTaskContext();

        workflowResult = applicationTimetableTimeValidationTask.execute(taskContext, workflowResult);
        assertThat(workflowResult.getErrors(), hasSize(1));
        assertThat(workflowResult.getErrors().get(0), is(ERROR_MSG_NOTICE_DATE_OR_TIME_REQUIRED));
        assertThat(taskContext.hasTaskFailed(), is(true));
    }

    @Test
    public void whenApplicationToBeConsideredHasHoursOrDays_thenNoErrorReturnedInResult() {
        WorkflowResult workflowResultWithHours = new WorkflowResult(ImmutableMap.of(
            IS_APPLICATION_URGENT, YES,
            APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS, ImmutableMap.of(
                HOURS, "48")));

        WorkflowResult workflowResult = applicationTimetableTimeValidationTask.execute(new DefaultTaskContext(), workflowResultWithHours);
        assertThat(workflowResult.getErrors(), hasSize(0));

        WorkflowResult workflowResultWithDays = new WorkflowResult(ImmutableMap.of(
            APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS, ImmutableMap.of(
                DAYS, "2")));

        workflowResult = applicationTimetableTimeValidationTask.execute(new DefaultTaskContext(), workflowResultWithDays);
        assertThat(workflowResult.getErrors(), hasSize(0));
    }

    @Test
    public void givenApplicationNotUrgent_whenNoApplicationToBeConsideredHoursOrDays_thenNoErrorReturnedInResult() {
        WorkflowResult workflowResult = new WorkflowResult(ImmutableMap.of(
            IS_APPLICATION_URGENT, NO,
            APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS, ImmutableMap.of()));
        TaskContext taskContext = new DefaultTaskContext();

        workflowResult = applicationTimetableTimeValidationTask.execute(taskContext, workflowResult);
        assertThat(workflowResult.getErrors(), hasSize(0));
        assertThat(taskContext.hasTaskFailed(), is(false));
    }
}
