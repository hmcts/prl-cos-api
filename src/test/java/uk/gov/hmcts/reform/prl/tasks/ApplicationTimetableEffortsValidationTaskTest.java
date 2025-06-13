package uk.gov.hmcts.reform.prl.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.framework.context.DefaultTaskContext;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICATION_NOTICE_EFFORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DAYS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HOURS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_APPLICATION_URGENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NO;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;
import static uk.gov.hmcts.reform.prl.tasks.ApplicationTimetableEffortsValidationTask.ERROR_MSG_NOTICE_EFFORTS_REQUIRED;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ApplicationTimetableEffortsValidationTask.class })
class ApplicationTimetableEffortsValidationTaskTest {

    @Autowired
    private ApplicationTimetableEffortsValidationTask applicationTimetableEffortsValidationTask;

    @Test
    void givenNoEffortsDetails_whenApplicationToBeConsideredInLessThan48Hours_thenErrorReturnedInResult() {
        WorkflowResult workflowResult = new WorkflowResult(ImmutableMap.of(
            IS_APPLICATION_URGENT, YES,
            APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS, ImmutableMap.of(
                DAYS, "1")));

        workflowResult = applicationTimetableEffortsValidationTask.execute(new DefaultTaskContext(), workflowResult);
        assertThat(workflowResult.getErrors(), hasSize(1));
        assertThat(workflowResult.getErrors().getFirst(), is(ERROR_MSG_NOTICE_EFFORTS_REQUIRED));
    }

    @Test
    void givenEffortsDetails_whenApplicationToBeConsideredInLessThan48Hours_thenNoErrorReturnedInResult() {
        WorkflowResult workflowResult = new WorkflowResult(ImmutableMap.of(
            IS_APPLICATION_URGENT, YES,
            APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS, ImmutableMap.of(
                DAYS, "1"),
            APPLICATION_NOTICE_EFFORTS, "efforts described"));

        workflowResult = applicationTimetableEffortsValidationTask.execute(new DefaultTaskContext(), workflowResult);
        assertThat(workflowResult.getErrors(), hasSize(0));
    }

    @Test
    void givenNoEffortsDetails_whenApplicationToBeConsideredIn48HoursOrMore_thenNoErrorReturnedInResult() {
        WorkflowResult workflowResult = new WorkflowResult(ImmutableMap.of(
            IS_APPLICATION_URGENT, YES,
            APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS, ImmutableMap.of(
                HOURS, "48")));

        workflowResult = applicationTimetableEffortsValidationTask.execute(new DefaultTaskContext(), workflowResult);
        assertThat(workflowResult.getErrors(), hasSize(0));
    }

    @Test
    void givenApplicationIsNotUrgentAndNoEffortsDetails_whenApplicationToBeConsideredInLessThan48Hours_thenNoErrorReturnedInResult() {
        WorkflowResult workflowResult = new WorkflowResult(ImmutableMap.of(
            IS_APPLICATION_URGENT, NO,
            APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS, ImmutableMap.of(
                DAYS, "1")));

        workflowResult = applicationTimetableEffortsValidationTask.execute(new DefaultTaskContext(), workflowResult);
        assertThat(workflowResult.getErrors(), hasSize(0));
    }
}
