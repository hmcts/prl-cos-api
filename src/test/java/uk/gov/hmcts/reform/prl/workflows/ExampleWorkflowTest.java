package uk.gov.hmcts.reform.prl.workflows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.tasks.emails.ExampleEmailTask;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.prl.utils.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.prl.utils.Verificators.verifyTaskWasCalled;

@ExtendWith(MockitoExtension.class)
class ExampleWorkflowTest {

    @Mock
    private ExampleEmailTask exampleEmailTask;

    @InjectMocks
    private ExampleWorkflow exampleWorkflow;

    @Test
    void shouldExecuteTask() throws Exception {
        CaseDetails caseDetails = CaseDetailsProvider.empty();
        mockTasksExecution(CaseDetailsProvider.empty(), exampleEmailTask);

        CaseDetails returned = exampleWorkflow.run(caseDetails);

        assertThat(returned, is(caseDetails));
        verifyTaskWasCalled(caseDetails, exampleEmailTask);
    }
}
