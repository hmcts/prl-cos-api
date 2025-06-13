
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ServiceOfDocumentsControllerTest {

    public static final String NO_DOCUMENTS_SELECTED_ERROR = "Please select a document or upload a document to serve";
    public static final String UN_SERVED_DOCUMENTS_PRESENT_ERROR =
        "Can not execute service of documents, there are unserved document(s) pending review";

    @InjectMocks
    private ServiceOfDocumentsController serviceOfDocumentsController;

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
    void testHandleAboutToStart() {
        when(serviceOfDocumentsService.handleAboutToStart(
            Mockito.anyString(),
            Mockito.any(CallbackRequest.class)
        )).thenReturn(caseDataMap);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfDocumentsController
            .handleAboutToStart(any(), any(), callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    void testHandleAboutToStartUnServedDocsPresent() {
        caseDataMap.put(
            "sodUnServedPack",
            SodPack.builder().documents(List.of(Element.<Document>builder().build())).build()
        );
        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataMap).build())
            .build();
        caseDataMap.put("errors", List.of(UN_SERVED_DOCUMENTS_PRESENT_ERROR));
        when(serviceOfDocumentsService.handleAboutToStart(anyString(), any(CallbackRequest.class))).thenReturn(
            caseDataMap);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfDocumentsController
            .handleAboutToStart(anyString(), anyString(), callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getErrors());
        assertEquals(UN_SERVED_DOCUMENTS_PRESENT_ERROR, aboutToStartOrSubmitCallbackResponse.getErrors().getFirst());
    }

    @Test
    void testHandleAboutToSubmit() {
        caseDataMap.put("sodUnServedPack", SodPack.builder().build());
        when(serviceOfDocumentsService.handleAboutToSubmit(
            Mockito.anyString(),
            Mockito.any(CallbackRequest.class)
        )).thenReturn(caseDataMap);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfDocumentsController
            .handleAboutToSubmit(any(), any(), callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    void testValidateDocumentsWhenEitherPresent() {
        when(serviceOfDocumentsService.validateDocuments(Mockito.any(CallbackRequest.class))).thenReturn(Collections.emptyList());

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfDocumentsController
            .validateDocuments(any(), any(), callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    void testValidateDocumentsWhenBothDocsAreEmpty() {
        List<String> errors = List.of(NO_DOCUMENTS_SELECTED_ERROR);
        when(serviceOfDocumentsService.validateDocuments(Mockito.any(CallbackRequest.class))).thenReturn(errors);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfDocumentsController
            .validateDocuments(any(), any(), callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getErrors());
        assertEquals(NO_DOCUMENTS_SELECTED_ERROR, aboutToStartOrSubmitCallbackResponse.getErrors().getFirst());
    }

    @Test
    void testHandleSubmitted() {
        ResponseEntity<SubmittedCallbackResponse> submittedCallbackResponse = ResponseEntity.ok().build();
        when(serviceOfDocumentsService.handleSubmitted(anyString(), Mockito.any(CallbackRequest.class))).thenReturn(
            submittedCallbackResponse);

        ResponseEntity<SubmittedCallbackResponse> response = serviceOfDocumentsController.handleSubmitted(
            anyString(),
            anyString(),
            callbackRequest
        );

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testExceptionHandleAboutToStart() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> {
                serviceOfDocumentsController.handleAboutToStart(any(), any(), callbackRequest);

            }
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionHandleAboutToSubmit() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> {
                serviceOfDocumentsController.handleAboutToSubmit(any(), any(), callbackRequest);
            }
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionHandleSubmitted() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> {
                serviceOfDocumentsController.handleSubmitted(any(), any(), callbackRequest);
            }
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionValidateDocuments() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> {
                serviceOfDocumentsController.validateDocuments(any(), any(), callbackRequest);
            }
        );

        assertEquals("Invalid Client", ex.getMessage());
    }
}
