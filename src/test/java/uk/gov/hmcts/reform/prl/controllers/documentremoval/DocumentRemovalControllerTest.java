package uk.gov.hmcts.reform.prl.controllers.documentremoval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.exception.InvalidClientException;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.documentremoval.DocumentRemovalService;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentRemovalControllerTest {

    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private DocumentRemovalService documentRemovalService;

    @InjectMocks
    private DocumentRemovalController controller;

    private final String auth = "Bearer token";
    private final String s2s = "S2S token";
    private final CallbackRequest callbackRequest = CallbackRequest.builder()
        .caseDetails(CaseDetails.builder().id(123L).build())
        .build();

    @Test
    void aboutToStart_authorized_returnsData() {
        when(authorisationService.isAuthorized(auth, s2s)).thenReturn(true);
        Map<String, Object> data = Map.of("key", "value");
        when(documentRemovalService.getCaseDocuments(any())).thenReturn(data);

        AboutToStartOrSubmitCallbackResponse response = controller.aboutToStart(auth, s2s, callbackRequest);

        assertEquals(data, response.getData());
        verify(documentRemovalService).getCaseDocuments(any());
    }

    @Test
    void aboutToStart_unauthorized_throwsException() {
        when(authorisationService.isAuthorized(auth, s2s)).thenReturn(false);
        assertThrows(InvalidClientException.class, () -> controller.aboutToStart(auth, s2s, callbackRequest));
    }

    @Test
    void midEvent_authorized_returnsData() {
        when(authorisationService.isAuthorized(auth, s2s)).thenReturn(true);
        Map<String, Object> data = Map.of("key", "value");
        when(documentRemovalService.getCaseDocumentSelectedForRemoval(any())).thenReturn(data);

        AboutToStartOrSubmitCallbackResponse response = controller.midEvent(auth, s2s, callbackRequest);

        assertEquals(data, response.getData());
        verify(documentRemovalService).getCaseDocumentSelectedForRemoval(any());
    }

    @Test
    void midEvent_unauthorized_throwsException() {
        when(authorisationService.isAuthorized(auth, s2s)).thenReturn(false);
        assertThrows(InvalidClientException.class, () -> controller.midEvent(auth, s2s, callbackRequest));
    }

    @Test
    void aboutToSubmit_authorized_returnsData() throws IOException {
        when(authorisationService.isAuthorized(auth, s2s)).thenReturn(true);
        Map<String, Object> data = Map.of("key", "value");
        when(documentRemovalService.removeDocumentFromCaseData(any())).thenReturn(data);

        AboutToStartOrSubmitCallbackResponse response = controller.aboutToSubmit(auth, s2s, callbackRequest);

        assertEquals(data, response.getData());
        verify(documentRemovalService).removeDocumentFromCaseData(any());
    }

    @Test
    void aboutToSubmit_unauthorized_throwsException() {
        when(authorisationService.isAuthorized(auth, s2s)).thenReturn(false);
        assertThrows(InvalidClientException.class, () -> controller.aboutToSubmit(auth, s2s, callbackRequest));
    }

    @Test
    void submitted_authorized_callsDeleteDocument() throws IOException {
        when(authorisationService.isAuthorized(auth, s2s)).thenReturn(true);

        SubmittedCallbackResponse response = controller.submitted(auth, s2s, callbackRequest);

        assertNotNull(response);
        verify(documentRemovalService).deleteDocument(any(CaseDetails.class));
        verify(documentRemovalService).executePostSubmittedActions(any(CallbackRequest.class));
    }

    @Test
    void submitted_unauthorized_throwsException() {
        when(authorisationService.isAuthorized(auth, s2s)).thenReturn(false);
        assertThrows(InvalidClientException.class, () -> controller.submitted(auth, s2s, callbackRequest));
    }
}
