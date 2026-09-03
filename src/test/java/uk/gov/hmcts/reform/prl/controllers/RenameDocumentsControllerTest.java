package uk.gov.hmcts.reform.prl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.renamedocument.RenameDocumentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RenameDocumentsControllerTest {

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private RenameDocumentService renameDocumentService;

    @InjectMocks
    private RenameDocumentsController renameDocumentsController;

    private static final String AUTH = "auth";
    private static final String S2S = "s2s";

    private CallbackRequest callbackRequest;

    @BeforeEach
    void setUp() {
        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(123L)
                .data(new HashMap<>())
                .build())
            .build();
    }

    @Test
    void testHandleAboutToStartSuccess() {
        when(authorisationService.isAuthorized(AUTH, S2S)).thenReturn(true);
        Map<String, Object> data = new HashMap<>();
        when(renameDocumentService.handleAboutToStart(AUTH, callbackRequest)).thenReturn(data);

        AboutToStartOrSubmitCallbackResponse response = renameDocumentsController.handleAboutToStart(AUTH, S2S, callbackRequest);

        assertNotNull(response);
        assertEquals(data, response.getData());
    }

    @Test
    void testHandleAboutToStartWithError() {
        when(authorisationService.isAuthorized(AUTH, S2S)).thenReturn(true);
        Map<String, Object> data = new HashMap<>();
        data.put("errors", List.of("error1"));
        when(renameDocumentService.handleAboutToStart(AUTH, callbackRequest)).thenReturn(data);

        AboutToStartOrSubmitCallbackResponse response = renameDocumentsController.handleAboutToStart(AUTH, S2S, callbackRequest);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertEquals("error1", response.getErrors().get(0));
    }

    @Test
    void testHandleAboutToStartUnauthorized() {
        when(authorisationService.isAuthorized(AUTH, S2S)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> 
            renameDocumentsController.handleAboutToStart(AUTH, S2S, callbackRequest)
        );
    }

    @Test
    void testHandleMidEventSuccess() {
        when(authorisationService.isAuthorized(AUTH, S2S)).thenReturn(true);
        Map<String, Object> data = new HashMap<>();
        when(renameDocumentService.handleMidEvent(AUTH, callbackRequest)).thenReturn(data);

        AboutToStartOrSubmitCallbackResponse response = renameDocumentsController.handleMidEvent(AUTH, S2S, callbackRequest);

        assertNotNull(response);
        assertEquals(data, response.getData());
    }

    @Test
    void testHandleMidEventUnauthorized() {
        when(authorisationService.isAuthorized(AUTH, S2S)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> 
            renameDocumentsController.handleMidEvent(AUTH, S2S, callbackRequest)
        );
    }

    @Test
    void testHandleValidateMidEvent() {
        List<String> errors = List.of("error1");
        when(renameDocumentService.validateRenamedField(any())).thenReturn(errors);

        AboutToStartOrSubmitCallbackResponse response = renameDocumentsController.handleValidateMidEvent(AUTH, callbackRequest);

        assertNotNull(response);
        assertEquals(errors, response.getErrors());
    }

    @Test
    void testHandleAboutToSubmitSuccess() {
        when(authorisationService.isAuthorized(AUTH, S2S)).thenReturn(true);
        Map<String, Object> data = new HashMap<>();
        when(renameDocumentService.handleAboutToSubmit(callbackRequest)).thenReturn(data);

        AboutToStartOrSubmitCallbackResponse response = renameDocumentsController.handleAboutToSubmit(AUTH, S2S, callbackRequest);

        assertNotNull(response);
        assertEquals(data, response.getData());
    }

    @Test
    void testHandleAboutToSubmitWithError() {
        when(authorisationService.isAuthorized(AUTH, S2S)).thenReturn(true);
        Map<String, Object> data = new HashMap<>();
        data.put("errors", List.of("error1"));
        when(renameDocumentService.handleAboutToSubmit(callbackRequest)).thenReturn(data);

        AboutToStartOrSubmitCallbackResponse response = renameDocumentsController.handleAboutToSubmit(AUTH, S2S, callbackRequest);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertEquals("error1", response.getErrors().get(0));
    }

    @Test
    void testHandleAboutToSubmitUnauthorized() {
        when(authorisationService.isAuthorized(AUTH, S2S)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> 
            renameDocumentsController.handleAboutToSubmit(AUTH, S2S, callbackRequest)
        );
    }
}
