package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.ResponseEntity.ok;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceOfApplicationControllerTest {

    @InjectMocks
    private ServiceOfApplicationController serviceOfApplicationController;

    @Mock
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Test
    public void testServiceOfApplicationAboutToStart() throws Exception {

        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        when(authorisationService.isAuthorized(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        when(serviceOfApplicationService.getSoaCaseFieldsMap(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfApplicationController
            .handleAboutToStart(authToken,s2sToken,callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    public void testHandleAboutToSubmit() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        when(authorisationService.isAuthorized(Mockito.anyString(),Mockito.anyString())).thenReturn(true);

        when(serviceOfApplicationService.handleAboutToSubmit(Mockito.any(CallbackRequest.class)))
            .thenReturn(caseData);
        assertNotNull(serviceOfApplicationController.handleAboutToSubmit(authToken,s2sToken,callbackRequest).getData());
    }

    @Test
    public void testHandleSubmitted() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        when(authorisationService.isAuthorized(Mockito.anyString(),Mockito.anyString())).thenReturn(true);

        when(serviceOfApplicationService.handleSoaSubmitted(Mockito.anyString(), Mockito.any(CallbackRequest.class)))
            .thenReturn(ok(
            SubmittedCallbackResponse.builder().confirmationHeader(
                "").confirmationBody(
                "").build()));
        assertNotNull(serviceOfApplicationController.handleSubmitted(authToken,s2sToken,callbackRequest));
    }

    @Test
    public void testHandleSubmittedInvalidClient() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        when(serviceOfApplicationService.handleSoaSubmitted(Mockito.anyString(), Mockito.any(CallbackRequest.class)))
            .thenReturn(ok(
                SubmittedCallbackResponse.builder().confirmationHeader(
                    "").confirmationBody(
                    "").build()));
        assertThrows(RuntimeException.class, () -> serviceOfApplicationController.handleAboutToSubmit(authToken,s2sToken,callbackRequest));
    }

    @Test
    public void testHandleAboutToStartInvalidClient() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        when(serviceOfApplicationService.handleSoaSubmitted(Mockito.anyString(), Mockito.any(CallbackRequest.class)))
            .thenReturn(ok(
                SubmittedCallbackResponse.builder().confirmationHeader(
                    "").confirmationBody(
                    "").build()));
        assertThrows(RuntimeException.class, () -> serviceOfApplicationController.handleAboutToStart(authToken,s2sToken,callbackRequest));
    }

    @Test
    public void testHandleAboutToSubmitInvalidClient() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        when(serviceOfApplicationService.handleSoaSubmitted(Mockito.anyString(), Mockito.any(CallbackRequest.class)))
            .thenReturn(ok(
                SubmittedCallbackResponse.builder().confirmationHeader(
                    "").confirmationBody(
                    "").build()));
        assertThrows(RuntimeException.class, () -> serviceOfApplicationController.handleSubmitted(authToken,s2sToken,callbackRequest));
    }
}
