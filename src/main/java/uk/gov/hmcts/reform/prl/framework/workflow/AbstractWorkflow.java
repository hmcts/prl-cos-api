package uk.gov.hmcts.reform.prl.framework.workflow;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.prl.framework.context.DefaultTaskContext;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.framework.task.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractWorkflow<T> implements Workflow<T> {
    private final ThreadLocal<DefaultTaskContext> threadLocalContext = new ThreadLocal<>();

    @Override
    public T execute(Task<T>[] tasks, DefaultTaskContext context, T payload, Pair... pairs) throws WorkflowException {
        threadLocalContext.set(context);

        for (Pair<Object, Object> pair : pairs) {
            getContext().setTransientObject(pair.getKey().toString(), pair.getValue());
        }

        return executeInternal(tasks, payload);
    }

    @Override
    public T execute(Task<T>[] tasks, T payload, Pair... pairs) throws WorkflowException {
        return execute(tasks, new DefaultTaskContext(), payload, pairs);
    }

    public DefaultTaskContext getContext() {
        return threadLocalContext.get();
    }

    @Override
    public Map<String, Object> errors() {
        Set<Map.Entry<String, Object>> entrySet = threadLocalContext.get().getTransientObjects().entrySet();
        Map<String, Object> errors = new HashMap<>();

        for (Map.Entry<String, Object> entry : entrySet) {
            String key = entry.getKey();
            if (key.endsWith("_Error")) {
                errors.put(key, entry.getValue());
            }
        }

        return errors;
    }

    protected abstract T executeInternal(Task[] tasks, T payload) throws WorkflowException;

    /**
     * Just to fix sonar issue.
     */
    public void unload() {
        this.threadLocalContext.remove();
    }
}
