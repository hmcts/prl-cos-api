package uk.gov.hmcts.reform.prl.framework.workflow;

import uk.gov.hmcts.reform.prl.framework.exceptions.TaskException;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.framework.task.Task;

public class DefaultWorkflow<T> extends AbstractWorkflow<T> {

    @SuppressWarnings("unchecked")
    @Override
    public T executeInternal(Task[] tasks, T payload) throws WorkflowException {
        try {
            for (Task<T> task : tasks) {
                if (getContext().hasTaskFailed()) {
                    break;
                }
                payload = task.execute(getContext(), payload);
            }
        } catch (TaskException e) {
            throw new WorkflowException(e.getMessage(), e);
        }

        return payload;
    }


}
