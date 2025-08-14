package uk.gov.hmcts.reform.prl.controllers.serviceofdocuments;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofdocuments.SodPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.serviceofdocuments.ServiceOfDocumentsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_SOA_C8_CHECK_APPROVED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceOfDocumentsConfCheckControllerTest {

    public static final String NO_DOCUMENTS_TO_REVIEW_ERROR = "There are no document(s) available for confidential check";

    @InjectMocks
    private ServiceOfDocumentsConfCheckController serviceOfDocumentsConfCheckController;

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
    public void testHandleAboutToStartNoDocsToReview() {
        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("There are no document(s) available for confidential check")).build();
        when(serviceOfDocumentsService.handleConfCheckAboutToStart(Mockito.anyString(), Mockito.any(CallbackRequest.class))).thenReturn(response);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfDocumentsConfCheckController
            .handleAboutToStart(anyString(), anyString(), callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getErrors());
        assertEquals(NO_DOCUMENTS_TO_REVIEW_ERROR, aboutToStartOrSubmitCallbackResponse.getErrors().get(0));
    }

    @Test
    public void testHandleAboutToStart() {
        caseDataMap.put("sodUnServedPack", SodPack.builder().documents(List.of(Element.<Document>builder().build())).build());
        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataMap).build();
        when(serviceOfDocumentsService.handleConfCheckAboutToStart(Mockito.anyString(), Mockito.any(CallbackRequest.class))).thenReturn(response);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfDocumentsConfCheckController
            .handleAboutToStart(anyString(), anyString(), callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    public void testHandleAboutToSubmit() {
        caseDataMap.put(WA_SOA_C8_CHECK_APPROVED, YES);
        when(serviceOfDocumentsService.handleConfCheckAboutToSubmit(Mockito.any(CallbackRequest.class))).thenReturn(caseDataMap);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfDocumentsConfCheckController
            .handleAboutToSubmit(anyString(), anyString(), callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    public void testHandleSubmitted() {
        ResponseEntity<SubmittedCallbackResponse> submittedCallbackResponse = ResponseEntity.ok().build();
        when(serviceOfDocumentsService.handleConfCheckSubmitted(anyString(), Mockito.any(CallbackRequest.class)))
            .thenReturn(submittedCallbackResponse);

        ResponseEntity<SubmittedCallbackResponse> response = serviceOfDocumentsConfCheckController
            .handleSubmitted(anyString(), anyString(), callbackRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testExceptionHandleAboutToStart() {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        assertExpectedException(() -> {
            serviceOfDocumentsConfCheckController.handleAboutToStart(any(), any(), callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionHandleAboutToSubmit() {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        assertExpectedException(() -> {
            serviceOfDocumentsConfCheckController.handleAboutToSubmit(any(), any(), callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionHandleSubmitted() {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        assertExpectedException(() -> {
            serviceOfDocumentsConfCheckController.handleSubmitted(any(), any(), callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

}
