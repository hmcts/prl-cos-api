package uk.gov.hmcts.reform.prl.workflows;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.exception.WorkflowException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.tasks.Task;
import uk.gov.hmcts.reform.prl.tasks.ValidateMiamApplicationOrExemptionTask;

@Component
@RequiredArgsConstructor
public class ValidateMiamApplicationOrExemptionWorkflow extends DefaultWorkflow<WorkflowResult> {

    private final ValidateMiamApplicationOrExemptionTask validateMiamApplicationOrExemptionTask;


    public WorkflowResult run(CallbackRequest callbackRequest) throws WorkflowException {

        return this.execute(
            new Task[] {
                validateMiamApplicationOrExemptionTask
            },
            new WorkflowResult(callbackRequest.getCaseDetails().getData())
        );
    }
}
