package uk.gov.hmcts.reform.prl.framework.workflow;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.prl.framework.context.DefaultTaskContext;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.framework.task.Task;

import java.util.Map;

public interface Workflow<T> {

    T execute(Task<T>[] tasks, T payload, Pair... pairs) throws WorkflowException;

    T execute(Task<T>[] tasks, DefaultTaskContext context, T payload, Pair... pairs) throws WorkflowException;

    Map<String, Object> errors();
}
