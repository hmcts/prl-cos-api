package uk.gov.hmcts.reform.prl.workflows;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.framework.task.Task;
import uk.gov.hmcts.reform.prl.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.tasks.CreatePaymentServiceRequestTask;

@Component
@RequiredArgsConstructor
public class CreatePaymentServiceRequestWorkflow extends DefaultWorkflow<WorkflowResult> {

    private final CreatePaymentServiceRequestTask createPaymentServiceRequestTask;


    public WorkflowResult run(CallbackRequest callbackRequest) throws WorkflowException {

        return this.execute(
            new Task[] {
                createPaymentServiceRequestTask
            },
            new WorkflowResult(callbackRequest.getCaseDetails().getData())
            );
    }
}
