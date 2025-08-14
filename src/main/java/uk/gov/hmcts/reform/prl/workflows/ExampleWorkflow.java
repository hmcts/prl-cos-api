package uk.gov.hmcts.reform.prl.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.framework.task.Task;
import uk.gov.hmcts.reform.prl.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.tasks.emails.ExampleEmailTask;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExampleWorkflow extends DefaultWorkflow<CaseDetails> {

    private final ExampleEmailTask exampleEmailTask;

    public CaseDetails run(CaseDetails caseDetails) throws WorkflowException {
        return this.execute(
            new Task[]{
                exampleEmailTask
            },
            caseDetails
        );
    }
}
