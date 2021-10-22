package uk.gov.hmcts.reform.prl.framework.task;

import uk.gov.hmcts.reform.prl.framework.context.TaskContext;
import uk.gov.hmcts.reform.prl.framework.exceptions.TaskException;

@FunctionalInterface
public interface Task<T> {
    T execute(TaskContext context, T payload) throws TaskException;
}
