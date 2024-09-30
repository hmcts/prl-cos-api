package uk.gov.hmcts.reform.prl.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
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
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofdocuments.SodPack;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ServiceOfDocumentsService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceOfDocumentsControllerTest {

    @InjectMocks
    private ServiceOfDocumentsController serviceOfDocumentsController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private ServiceOfDocumentsService serviceOfDocumentsService;

    private CallbackRequest callbackRequest;
    private Map<String, Object> caseDataMap;

    @Before
    public void setUp() {
        caseDataMap = new HashMap<>();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataMap).build())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
    }

    @Test
    public void testHandleAboutToStart() {
        when(serviceOfDocumentsService.handleAboutToStart(Mockito.anyString(), Mockito.any(CallbackRequest.class))).thenReturn(caseDataMap);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfDocumentsController
            .handleAboutToStart(any(),any(),callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    public void testExceptionHandleAboutToStart() {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        assertExpectedException(() -> {
            serviceOfDocumentsController.handleAboutToStart(any(), any(), callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testHandleAboutToSubmit() {
        caseDataMap.put("sodUnServedPack", SodPack.builder().build());
        when(serviceOfDocumentsService.handleAboutToSubmit(Mockito.anyString(), Mockito.any(CallbackRequest.class))).thenReturn(caseDataMap);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfDocumentsController
            .handleAboutToSubmit(any(),any(),callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    public void testExceptionHandleAboutToSubmit() {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        assertExpectedException(() -> {
            serviceOfDocumentsController.handleAboutToSubmit(any(), any(), callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    /*@Test
    public void testHandleSubmitted() throws Exception {
        when(serviceOfDocumentsService.handleSubmitted(Mockito.anyString(), Mockito.any(CallbackRequest.class))).thenReturn(
            ok(SubmittedCallbackResponse.builder().build()));

        assertNotNull(serviceOfDocumentsController.handleSubmitted(any(),any(),callbackRequest));
    }*/

    @Test
    public void testExceptionHandleSubmitted() {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        assertExpectedException(() -> {
            serviceOfDocumentsController.handleSubmitted(any(), any(), callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
