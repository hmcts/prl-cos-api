package uk.gov.hmcts.reform.prl.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mockito.InOrder;
import uk.gov.hmcts.reform.prl.framework.context.TaskContext;
import uk.gov.hmcts.reform.prl.framework.exceptions.TaskException;
import uk.gov.hmcts.reform.prl.framework.task.Task;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Verificators {

    public static void verifyTaskWasCalled(CaseDetails caseDetails, Task<CaseDetails> task) throws TaskException {
        verify(task).execute(any(TaskContext.class), eq(caseDetails));
    }

    public static void verifyTaskWasNeverCalled(Task<CaseDetails> task) throws TaskException {
        verify(task, never()).execute(any(TaskContext.class), any(CaseDetails.class));
    }

    public static void verifyTasksWereNeverCalled(Task<CaseDetails>... tasks) throws TaskException {
        for (Task<CaseDetails> task : tasks) {
            verify(task, never()).execute(any(TaskContext.class), any(CaseDetails.class));
        }
    }

    public static void verifyTasksCalledInOrder(CaseDetails caseDetails, Object... tasks) throws TaskException {
        InOrder inOrder = inOrder(tasks);

        for (Object task : tasks) {
            inOrder.verify((Task<CaseDetails>) task).execute(any(TaskContext.class), eq(caseDetails));
        }
    }

    @SafeVarargs
    public static void mockTasksExecution(
        CaseDetails caseDetails, Task<CaseDetails>... tasksToMock) throws TaskException {
        for (Task<CaseDetails> task : tasksToMock) {
            when(task.execute(any(TaskContext.class), eq(caseDetails))).thenReturn(caseDetails);
        }
    }
}
