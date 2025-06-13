package uk.gov.hmcts.reform.prl.framework.workflow;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.framework.context.DefaultTaskContext;
import uk.gov.hmcts.reform.prl.framework.exceptions.TaskException;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.framework.task.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DefaultWorkflowTest {

    private DefaultWorkflow<String> defaultWorkflow;
    private String payload;

    @BeforeEach
    void setup() {
        defaultWorkflow = new DefaultWorkflow<>();
        payload = "";
    }

    @Test
    void executeShouldReturnTheModifiedPayloadAfterRunningTasks() throws Exception {
        Task<String> taskOne = (context, payload) -> payload.concat("1");
        Task<String> taskTwo = (context, payload) -> payload.concat("2");
        Task<String> taskThree = (context, payload) -> payload.concat("3");

        Task[] tasks = new Task[] {
            taskOne, taskTwo, taskThree
        };

        assertEquals("123", defaultWorkflow.execute(tasks, payload));
    }

    @Test
    void executeShouldStopAfterContextStatusIsSetToFailed() throws Exception {
        Task<String> taskOne = (context, payload) -> payload.concat("1");
        Task<String> taskTwo = (context, payload) -> {
            context.setTaskFailed(true);
            return payload.concat("2");
        };
        Task<String> taskThree = (context, payload) -> payload.concat("3");

        Task[] tasks = new Task[] {
            taskOne, taskTwo, taskThree
        };

        assertEquals("12", defaultWorkflow.execute(tasks, payload));
    }

    @Test
    void executeShouldStopIfContextStatusIsSetToFailed() throws Exception {
        Task<String> taskOne = (context, payload) -> payload.concat("1");
        Task<String> taskTwo = (context, payload) -> payload.concat("2");
        Task<String> taskThree = (context, payload) -> payload.concat("3");

        Task[] tasks = new Task[] {
            taskOne, taskTwo, taskThree
        };
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTaskFailed(true);
        assertEquals(payload, defaultWorkflow.execute(tasks, context, payload));
    }

    @Test
    void executeWillSetOptionalPairsIntoTheContext() throws Exception {
        Task<String> task = (context, payload) -> context.getTransientObject("testKey").toString();

        Task[] tasks = new Task[] { task };

        Pair pair = new ImmutablePair<>("testKey", "testValue");

        assertEquals("testValue", defaultWorkflow.execute(tasks, payload, pair));
    }

    @Test
    void executeShouldThrowExceptionWithNoTasks() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            defaultWorkflow.execute(null, payload);
        });
    }

    @Test
    void executeShouldThrowExceptionWhenATaskExceptionIsThrown() throws Exception {
        Task[] tasks = new Task[] {
            (context, payload) -> {
                throw new TaskException("Error");
            }
        };

        assertThrows(WorkflowException.class, () -> {
            defaultWorkflow.execute(tasks, payload);
        });
    }

    @Test
    void errorsShouldReturnEmptyListWhenNoErrorsAreInContext() throws Exception {
        Task<String> taskOne = (context, payload) -> {
            context.setTransientObject("hello", "world");
            return payload;
        };

        Task[] tasks = new Task[] {
            taskOne
        };

        defaultWorkflow.execute(tasks, payload);

        assertEquals(0, defaultWorkflow.errors().size());
    }

    @Test
    void errorsShouldReturnListOfErrorsWhenErrorsAreInContext() throws Exception {
        Task<String> taskOne = (context, payload) -> {
            context.setTransientObject("one_Error", "error");
            return payload;
        };

        Task<String> taskTwo = (context, payload) -> {
            context.setTransientObject("two_Error", "error");
            return payload;
        };

        Task[] tasks = new Task[] {
            taskOne, taskTwo
        };

        defaultWorkflow.execute(tasks, payload);

        assertEquals(2, defaultWorkflow.errors().size());
    }
}
