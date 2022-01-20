package uk.gov.hmcts.reform.prl.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.tasks.ReturnApplicaitonReturnMessageTask;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.prl.utils.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.prl.utils.Verificators.verifyTaskWasCalled;

@RunWith(MockitoJUnitRunner.class)
public class ReturnApplicationReturnMessageWorkflowTest {

    @Mock
    private ReturnApplicaitonReturnMessageTask returnMessageTask;

    @InjectMocks
    private ReturnApplicationReturnMessageWorkflow returnMessageWorkflow;

    @Test
    public void shouldExecuteTask() throws Exception {
        CaseDetails caseDetails = CaseDetailsProvider.empty();
        mockTasksExecution(CaseDetailsProvider.empty(), returnMessageTask);

        CaseDetails returned = returnMessageWorkflow.run(caseDetails);

        assertThat(returned, is(caseDetails));
        verifyTaskWasCalled(caseDetails, returnMessageTask);
    }
}
