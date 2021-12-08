package uk.gov.hmcts.reform.prl.workflows;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.framework.task.Task;
import uk.gov.hmcts.reform.prl.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.tasks.ServiceRequestUpdateTask;

@Component
@RequiredArgsConstructor
public class ServiceRequestUpdateWorkflow extends DefaultWorkflow<WorkflowResult> {

    private final ServiceRequestUpdateTask serviceRequestUpdateTask;


    public WorkflowResult run(CallbackRequest callbackRequest) throws WorkflowException {

        return this.execute(
            new Task[] {
                serviceRequestUpdateTask
            },
            new WorkflowResult(callbackRequest.getCaseDetails().getData())
            );
    }
}
