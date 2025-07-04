package uk.gov.hmcts.reform.prl.tasks;

import uk.gov.hmcts.reform.prl.exception.TaskException;

@FunctionalInterface
public interface Task<T> {
    T execute(TaskContext context, T payload) throws TaskException;
}
