package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.services.ExampleService;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;
import uk.gov.hmcts.reform.prl.workflows.ValidateMiamApplicationOrExemptionWorkflow;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class CallbackControllerTest {

    @Mock
    private ExampleService exampleService;

    @Mock
    private ValidateMiamApplicationOrExemptionWorkflow validateMiamApplicationOrExemptionWorkflow;

    @InjectMocks
    private CallbackController callbackController;

    @Mock
    private WorkflowResult workflowResult;

    @Test
    public void testSendEmail() throws WorkflowException {
        CaseDetails caseDetails = CaseDetailsProvider.full();
        when(exampleService.executeExampleWorkflow(caseDetails)).thenReturn(caseDetails.getCaseData());

        callbackController.sendEmail(CallbackRequest.builder().caseDetails(caseDetails).build());

        verify(exampleService).executeExampleWorkflow(caseDetails);
        verifyNoMoreInteractions(exampleService);
    }

    @Test
    public void testConfirmMiamApplicationOrExemption() throws WorkflowException {
        CaseDetails caseDetails  = CaseDetailsProvider.full();

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model.CallbackRequest.builder().build();


        when(validateMiamApplicationOrExemptionWorkflow.run(callbackRequest))
            .thenReturn(workflowResult);

        callbackController.validateMiamApplicationOrExemption(callbackRequest);

        verify(validateMiamApplicationOrExemptionWorkflow).run(callbackRequest);
        verifyNoMoreInteractions(validateMiamApplicationOrExemptionWorkflow);

    }
}
