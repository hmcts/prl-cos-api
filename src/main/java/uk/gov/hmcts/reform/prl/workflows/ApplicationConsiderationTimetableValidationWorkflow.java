package uk.gov.hmcts.reform.prl.workflows;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.framework.task.Task;
import uk.gov.hmcts.reform.prl.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.tasks.ApplicationTimetableEffortsValidationTask;
import uk.gov.hmcts.reform.prl.tasks.ApplicationTimetableTimeValidationTask;

@Component
@RequiredArgsConstructor
public class ApplicationConsiderationTimetableValidationWorkflow extends DefaultWorkflow<WorkflowResult> {

    private final ApplicationTimetableTimeValidationTask applicationTimetableTimeValidationTask;
    private final ApplicationTimetableEffortsValidationTask applicationTimetableEffortsValidationTask;
    private final ObjectMapper objectMapper;

    public WorkflowResult run(CallbackRequest callbackRequest) throws WorkflowException {
        return this.execute(
            new Task[]{
                applicationTimetableTimeValidationTask,
                applicationTimetableEffortsValidationTask
            },
            new WorkflowResult(callbackRequest.getCaseDetails().getData())
        );
    }
}
