package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.*;
import uk.gov.hmcts.reform.prl.services.DgsService;
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

    @Mock
    private DgsService dgsService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String PRL_DRAFT_TEMPLATE = "FL-DIV-GOR-ENG-00062.docx";

    @Before
    public void setUp() {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
    }

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

    @Test(expected = NullPointerException.class)
    public void testGenerateAndStoreDocument() throws Exception {
        //CaseDetails caseDetails  = CaseDetailsProvider.full();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(CaseData.builder()
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("FL-DIV-GOR-ENG-00062.docx")
                               .build())
            .build())
            .build();

        CallbackResponse callbackResponse = CallbackResponse.builder()
            .data(CaseData.builder()
                      .draftOrderDoc(Document.builder()
                                         .documentUrl(generatedDocumentInfo.getUrl())
                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                         .documentHash(generatedDocumentInfo.getHashToken())
                                         .documentFileName("FL-DIV-GOR-ENG-00062.docx")
                                         .build())
                      .build())
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().build();

        when(dgsService.generateDocument(authToken,null,PRL_DRAFT_TEMPLATE))
            .thenReturn(generatedDocumentInfo);

        callbackController.generateAndStoreDocument(authToken, callbackRequest);

        verify(dgsService).generateDocument(authToken,null,PRL_DRAFT_TEMPLATE);
        verifyNoMoreInteractions(dgsService);

    }
}
