package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertNull;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ConfidentialityCheckControllerTest {

    @InjectMocks
    private ConfidentialityCheckController confidentialityCheckController;

    @Mock
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    private ObjectMapper objectMapper;

    @Ignore
    @Test
    public void testServiceOfApplicationAboutToStart() throws Exception {

        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        CaseData caseData1 = CaseData.builder().build();
        when(serviceOfApplicationService.getSoaCaseFieldsMap(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = confidentialityCheckController
            .confidentialCheckAboutToStart(callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    public void testHandleAboutToSubmit() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();

        ResponseEntity<SubmittedCallbackResponse> submittedCallbackResponse = ResponseEntity.noContent().build();
        when(serviceOfApplicationService.processConfidentialityCheck("", callbackRequest)).thenReturn(submittedCallbackResponse);
        assertNull(confidentialityCheckController.handleSubmittedNew("test auth",callbackRequest));
    }
}
