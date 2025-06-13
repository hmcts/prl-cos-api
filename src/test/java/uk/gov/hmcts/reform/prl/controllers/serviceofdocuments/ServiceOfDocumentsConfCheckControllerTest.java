package uk.gov.hmcts.reform.prl.controllers.serviceofdocuments;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_SOA_C8_CHECK_APPROVED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ServiceOfDocumentsConfCheckControllerTest {

    public static final String NO_DOCUMENTS_TO_REVIEW_ERROR = "There are no document(s) available for confidential check";

    @InjectMocks
    private ServiceOfDocumentsConfCheckController serviceOfDocumentsConfCheckController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private ServiceOfDocumentsService serviceOfDocumentsService;

    private CallbackRequest callbackRequest;
    private Map<String, Object> caseDataMap;

    @BeforeEach
    void setUp() {
        caseDataMap = new HashMap<>();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataMap).build())
            .build();

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
    }

    @Test
    void testHandleAboutToStartNoDocsToReview() {
        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("There are no document(s) available for confidential check")).build();
        when(serviceOfDocumentsService.handleConfCheckAboutToStart(
            Mockito.anyString(),
            Mockito.any(CallbackRequest.class)
        )).thenReturn(response);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfDocumentsConfCheckController
            .handleAboutToStart(anyString(), anyString(), callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getErrors());
        assertEquals(NO_DOCUMENTS_TO_REVIEW_ERROR, aboutToStartOrSubmitCallbackResponse.getErrors().getFirst());
    }

    @Test
    void testHandleAboutToStart() {
        caseDataMap.put(
            "sodUnServedPack",
            SodPack.builder().documents(List.of(Element.<Document>builder().build())).build()
        );
        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataMap).build();
        when(serviceOfDocumentsService.handleConfCheckAboutToStart(
            Mockito.anyString(),
            Mockito.any(CallbackRequest.class)
        )).thenReturn(response);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfDocumentsConfCheckController
            .handleAboutToStart(anyString(), anyString(), callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    void testHandleAboutToSubmit() {
        caseDataMap.put(WA_SOA_C8_CHECK_APPROVED, YES);
        when(serviceOfDocumentsService.handleConfCheckAboutToSubmit(Mockito.any(CallbackRequest.class))).thenReturn(
            caseDataMap);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfDocumentsConfCheckController
            .handleAboutToSubmit(anyString(), anyString(), callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    void testHandleSubmitted() {
        ResponseEntity<SubmittedCallbackResponse> submittedCallbackResponse = ResponseEntity.ok().build();
        when(serviceOfDocumentsService.handleConfCheckSubmitted(anyString(), Mockito.any(CallbackRequest.class)))
            .thenReturn(submittedCallbackResponse);

        ResponseEntity<SubmittedCallbackResponse> response = serviceOfDocumentsConfCheckController
            .handleSubmitted(anyString(), anyString(), callbackRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testExceptionHandleAboutToStart() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> {
                serviceOfDocumentsConfCheckController.handleAboutToStart(any(), any(), callbackRequest);
            }
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionHandleAboutToSubmit() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> {
                serviceOfDocumentsConfCheckController.handleAboutToSubmit(any(), any(), callbackRequest);
            }
        );

        assertEquals("Invalid Client", ex.getMessage());

    }

    @Test
    void testExceptionHandleSubmitted() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> {
                serviceOfDocumentsConfCheckController.handleSubmitted(any(), any(), callbackRequest);
            }
        );

        assertEquals("Invalid Client", ex.getMessage());
    }
}
