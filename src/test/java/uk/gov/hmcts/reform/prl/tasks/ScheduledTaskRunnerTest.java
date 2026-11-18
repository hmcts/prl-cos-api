package uk.gov.hmcts.reform.prl.tasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ScheduledTaskRunnerTest {

    @Mock
    private ApplicationContext context;

    @Mock
    private Runnable task;

    @InjectMocks
    private ScheduledTaskRunner taskRunner;

    @Test
    void shouldFindTheBean() {
        when(context.getBean("lowerCaseBean")).thenReturn(task);

        taskRunner.run("LowerCaseBean");

        verify(task).run();
    }

    @Test
    void shouldNotFindTheBean() {
        when(context.getBean("missingBean")).thenThrow();

        taskRunner.run("missingBean");

        verifyNoInteractions(task);
    }

    @Test
    void shouldCatchExceptionWhenTaskRunThrows() {
        ApplicationContext context = mock(ApplicationContext.class);
        Runnable failingTask = mock(Runnable.class);

        when(context.getBean("failingTask")).thenReturn(failingTask);
        org.mockito.Mockito.doThrow(new RuntimeException("boom"))
            .when(failingTask).run();

        ScheduledTaskRunner runner = new ScheduledTaskRunner(context);

        Executable call = () -> runner.run("FailingTask");

        assertDoesNotThrow(call);
        verify(context).getBean("failingTask");
        verify(failingTask).run();
    }
}
