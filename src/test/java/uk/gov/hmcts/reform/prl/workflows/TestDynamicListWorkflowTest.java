package uk.gov.hmcts.reform.prl.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.prl.framework.context.DefaultTaskContext;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.workflows.TestDynamicListWorkflow;
import uk.gov.hmcts.reform.prl.workflows.ValidateMiamApplicationOrExemptionWorkflow;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.prl.models.OrchestrationConstants.APPLICANT_ATTENDED_MIAM;
import static uk.gov.hmcts.reform.prl.models.OrchestrationConstants.CLAIMING_EXEMPTION_MIAM;
import static uk.gov.hmcts.reform.prl.models.OrchestrationConstants.NO;
import static uk.gov.hmcts.reform.prl.models.OrchestrationConstants.YES;
import static uk.gov.hmcts.reform.prl.tasks.ValidateMiamApplicationOrExemptionTask.ERROR_MSG_MIAM;

public class TestDynamicListWorkflowTest {

    @Mock
    private TestDynamicListTask testDynamicListTask;

    @InjectMocks
    private TestDynamicListWorkflow testDynamicListWorkflow;

    @Test
    public void give_when_then() {

        assert(true);
    }
}
