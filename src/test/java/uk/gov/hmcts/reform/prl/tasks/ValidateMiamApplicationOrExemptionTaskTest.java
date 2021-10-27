package uk.gov.hmcts.reform.prl.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.framework.context.DefaultTaskContext;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.prl.models.OrchestrationConstants.APPLICANT_ATTENDED_MIAM;
import static uk.gov.hmcts.reform.prl.models.OrchestrationConstants.CLAIMING_EXEMPTION_MIAM;
import static uk.gov.hmcts.reform.prl.models.OrchestrationConstants.NO;
import static uk.gov.hmcts.reform.prl.models.OrchestrationConstants.YES;
import static uk.gov.hmcts.reform.prl.tasks.ValidateMiamApplicationOrExemptionTask.ERROR_MSG_MIAM;

public class ValidateMiamApplicationOrExemptionTaskTest {

    private ValidateMiamApplicationOrExemptionTask validateMiamApplicationOrExemptionTask =
        new ValidateMiamApplicationOrExemptionTask();

    @Test
    public void givenApplicantHasNotAttendedMiam_whenApplicantDoesNotHaveMiamExemption_thenErrorReturnedInResult() {

        WorkflowResult workflowData = new WorkflowResult(ImmutableMap.of(
            APPLICANT_ATTENDED_MIAM, NO,
            CLAIMING_EXEMPTION_MIAM, NO));

        WorkflowResult workflowResult = validateMiamApplicationOrExemptionTask.execute(new DefaultTaskContext(), workflowData);

        assertThat(workflowResult.getErrors().get(0), is(ERROR_MSG_MIAM));
    }

    @Test
    public void givenApplicantHasAttendedMiam_whenApplicantDoesNotHaveMiamExemption_thenNoErrorReturnedInResult() {

        WorkflowResult workflowData = new WorkflowResult(ImmutableMap.of(
            APPLICANT_ATTENDED_MIAM, YES,
            CLAIMING_EXEMPTION_MIAM, NO));

        WorkflowResult workflowResult = validateMiamApplicationOrExemptionTask.execute(new DefaultTaskContext(), workflowData);

        assertThat(workflowResult.getErrors(), hasSize(0));
    }

    @Test
    public void givenApplicantHasNotAttendedMiam_whenApplicantDoesHaveMiamExemption_thenNoErrorReturnedInResult() {

        WorkflowResult workflowData = new WorkflowResult(ImmutableMap.of(
            APPLICANT_ATTENDED_MIAM, NO,
            CLAIMING_EXEMPTION_MIAM, YES));

        WorkflowResult workflowResult = validateMiamApplicationOrExemptionTask.execute(new DefaultTaskContext(), workflowData);

        assertThat(workflowResult.getErrors(), hasSize(0));
    }
}
