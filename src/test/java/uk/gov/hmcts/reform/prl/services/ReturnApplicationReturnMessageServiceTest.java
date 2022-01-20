package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.utils.CaseDataProvider;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;
import uk.gov.hmcts.reform.prl.workflows.ReturnApplicationReturnMessageWorkflow;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReturnApplicationReturnMessageServiceTest {
    @Mock
    private ReturnApplicationReturnMessageWorkflow returnApplicationReturnMessageWorkflow;

    @InjectMocks
    private ReturnApplicationReturnMessageService returnMessageService;

    @Test
    public void testExecuteReturnMessageWorkflow() throws WorkflowException {
        CaseDetails caseDetails = CaseDetailsProvider.of(CaseDataProvider.empty());
        when(returnApplicationReturnMessageWorkflow.run(caseDetails)).thenReturn(caseDetails);

        returnMessageService.executeReturnMessageWorkflow(caseDetails);

        verify(returnApplicationReturnMessageWorkflow).run(any(CaseDetails.class));
        verifyNoMoreInteractions(returnApplicationReturnMessageWorkflow);
    }
}
