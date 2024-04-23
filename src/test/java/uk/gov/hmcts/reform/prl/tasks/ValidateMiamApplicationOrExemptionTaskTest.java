package uk.gov.hmcts.reform.prl.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.framework.context.DefaultTaskContext;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_ATTENDED_MIAM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CLAIMING_EXEMPTION_MIAM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MPU_APPLICANT_ATTENDED_MIAM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MPU_CHILD_INVOLVED_IN_MIAM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MPU_CLAIMING_EXEMPTION_MIAM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NO;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;
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

    @Test
    public void testV3ErrorMessageChildInvolved() {

        WorkflowResult workflowData = new WorkflowResult(ImmutableMap.of(
            "taskListVersion", "v3",
            MPU_CHILD_INVOLVED_IN_MIAM, YES,
            MPU_APPLICANT_ATTENDED_MIAM, NO,
            MPU_CLAIMING_EXEMPTION_MIAM, NO));

        WorkflowResult workflowResult = validateMiamApplicationOrExemptionTask.execute(new DefaultTaskContext(), workflowData);

        assertThat(workflowResult.getErrors(), hasSize(0));
    }

    @Test
    public void testV3ErrorMessageApplicantAttended() {

        WorkflowResult workflowData = new WorkflowResult(ImmutableMap.of(
            "taskListVersion", "v3",
            MPU_CHILD_INVOLVED_IN_MIAM, NO,
            MPU_APPLICANT_ATTENDED_MIAM, YES,
            MPU_CLAIMING_EXEMPTION_MIAM, NO));

        WorkflowResult workflowResult = validateMiamApplicationOrExemptionTask.execute(new DefaultTaskContext(), workflowData);

        assertThat(workflowResult.getErrors(), hasSize(0));
    }

    @Test
    public void testV3ErrorMessageClaimingExemption() {

        WorkflowResult workflowData = new WorkflowResult(ImmutableMap.of(
            "taskListVersion", "v3",
            MPU_CHILD_INVOLVED_IN_MIAM, NO,
            MPU_APPLICANT_ATTENDED_MIAM, NO,
            MPU_CLAIMING_EXEMPTION_MIAM, YES));

        WorkflowResult workflowResult = validateMiamApplicationOrExemptionTask.execute(new DefaultTaskContext(), workflowData);

        assertThat(workflowResult.getErrors(), hasSize(0));
    }

    @Test
    public void testV3ErrorMessageThrowsError() {

        WorkflowResult workflowData = new WorkflowResult(ImmutableMap.of(
            "taskListVersion", "v3",
            MPU_CHILD_INVOLVED_IN_MIAM, NO,
            MPU_APPLICANT_ATTENDED_MIAM, NO,
            MPU_CLAIMING_EXEMPTION_MIAM, NO));

        WorkflowResult workflowResult = validateMiamApplicationOrExemptionTask.execute(new DefaultTaskContext(), workflowData);

        assertThat(workflowResult.getErrors().get(0), is(ERROR_MSG_MIAM));
    }
}
