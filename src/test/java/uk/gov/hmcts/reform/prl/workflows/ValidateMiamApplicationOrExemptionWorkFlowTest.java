package uk.gov.hmcts.reform.prl.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.tasks.ValidateMiamApplicationOrExemptionTask;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ValidateMiamApplicationOrExemptionWorkFlowTest {

    @Mock private ValidateMiamApplicationOrExemptionTask validateMiamApplicationOrExemptionTask;

    @InjectMocks
    private ValidateMiamApplicationOrExemptionWorkflow validateMiamApplicationOrExemptionWorkflow;

    @Test
    public void whenWorkflowRun_thenExpectedTasksInvoked() throws WorkflowException {
        validateMiamApplicationOrExemptionWorkflow.run(
            CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                                 .data(ImmutableMap.of())
                                 .build())
                .build());

        verify(validateMiamApplicationOrExemptionTask).execute(any(), any());
    }
}
