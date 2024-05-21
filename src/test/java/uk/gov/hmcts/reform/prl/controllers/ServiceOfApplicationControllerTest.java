package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        when(serviceOfApplicationService.getSoaCaseFieldsMap(Mockito.anyString(),Mockito.any(CaseDetails.class))).thenReturn(caseData);
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
    public void testHandleSubmittedInvalidClient() {
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
    public void testHandleAboutToStartInvalidClient() {
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
    public void testHandleAboutToSubmitInvalidClient() {
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

    @Test
    public void testExceptionForHandleAboutToSubmit() {
        CaseData cd = CaseData.builder()
            .caseInvites(Collections.emptyList())
            .build();

        Map<String, Object> caseData = new HashMap<>();
        final CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        when(objectMapper.convertValue(cd,  Map.class)).thenReturn(caseData);
        assertExpectedException(() -> {
            serviceOfApplicationController.handleSubmitted(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void handleAboutToSubmit() {
        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails
                             .builder().data(caseData)
                             .build())
            .build();
        when(authorisationService.isAuthorized(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfApplicationController
            .handleAboutToSubmit("","", callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse);
    }

    @Test
    public void handleValidateSoa() {
        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails
                             .builder().data(caseData)
                             .build())
            .build();
        when(serviceOfApplicationService.soaValidation(Mockito.any())).thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());
        when(authorisationService.isAuthorized(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfApplicationController
            .soaValidation("","", callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse);
    }

    @Test
    public void testExceptionForSoaValidation() {

        final CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(new HashMap<>()).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        assertExpectedException(() -> {
            serviceOfApplicationController.soaValidation(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
