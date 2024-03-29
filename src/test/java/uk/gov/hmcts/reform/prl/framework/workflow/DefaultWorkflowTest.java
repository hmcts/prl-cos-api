package uk.gov.hmcts.reform.prl.framework.workflow;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.framework.context.DefaultTaskContext;
import uk.gov.hmcts.reform.prl.framework.exceptions.TaskException;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.framework.task.Task;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DefaultWorkflowTest {

    private DefaultWorkflow<String> defaultWorkflow;
    private String payload;

    @Before
    public void setup() {
        defaultWorkflow = new DefaultWorkflow<>();
        payload = "";
    }

    @Test
    public void executeShouldReturnTheModifiedPayloadAfterRunningTasks() throws Exception {
        Task<String> taskOne = (context, payload) -> payload.concat("1");
        Task<String> taskTwo = (context, payload) -> payload.concat("2");
        Task<String> taskThree = (context, payload) -> payload.concat("3");

        Task[] tasks = new Task[] {
            taskOne, taskTwo, taskThree
        };

        assertEquals("123", defaultWorkflow.execute(tasks, payload));
    }

    @Test
    public void executeShouldStopAfterContextStatusIsSetToFailed() throws Exception {
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
    public void executeShouldStopIfContextStatusIsSetToFailed() throws Exception {
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
    public void executeWillSetOptionalPairsIntoTheContext() throws Exception {
        Task<String> task = (context, payload) -> context.getTransientObject("testKey").toString();

        Task[] tasks = new Task[] { task };

        Pair pair = new ImmutablePair<>("testKey", "testValue");

        assertEquals("testValue", defaultWorkflow.execute(tasks, payload, pair));
    }

    @Test(expected = NullPointerException.class)
    public void executeShouldThrowExceptionWithNoTasks() throws Exception {
        defaultWorkflow.execute(null, payload);
    }

    @Test(expected = WorkflowException.class)
    public void executeShouldThrowExceptionWhenATaskExceptionIsThrown() throws Exception {
        Task[] tasks = new Task[] { (context, payload) -> {
            throw new TaskException("Error"); }
        };
        defaultWorkflow.execute(tasks, payload);
    }

    @Test
    public void errorsShouldReturnEmptyListWhenNoErrorsAreInContext() throws Exception {
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
    public void errorsShouldReturnListOfErrorsWhenErrorsAreInContext() throws Exception {
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
