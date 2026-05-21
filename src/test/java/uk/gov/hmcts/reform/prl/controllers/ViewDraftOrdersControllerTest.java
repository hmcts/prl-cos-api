package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.exception.InvalidClientException;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ViewDraftOrdersService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.controllers.ViewDraftOrdersController.READONLY_DRAFT_ORDERS_MESSAGE;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ViewDraftOrdersControllerTest {

    @InjectMocks
    private ViewDraftOrdersController viewDraftOrdersController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private ViewDraftOrdersService viewDraftOrdersService;

    @Mock
    private Map<String, Object> caseFieldsMap;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";

    @Test
    public void testCallBackUrlAboutToStartEventFailsAuthorisation() {
        // Given
        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);

        // When
        InvalidClientException exception = assertThrows(InvalidClientException.class, () ->
            viewDraftOrdersController.callBackUrlAboutToStartEvent(AUTH_TOKEN, S2S_TOKEN, callbackRequest));

        // Then
        assertEquals(INVALID_CLIENT, exception.getMessage());
    }

    @Test
    public void testCallBackUrlAboutToStartEvent() {
        // Given
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1L)
            .data(Collections.emptyMap()).build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails).build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        when(viewDraftOrdersService.getViewDraftOrdersCaseFieldsMap(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(caseFieldsMap);

        // When
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            viewDraftOrdersController.callBackUrlAboutToStartEvent(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        // Then
        assertEquals(caseFieldsMap, aboutToStartOrSubmitCallbackResponse.getData());
        assertNull(aboutToStartOrSubmitCallbackResponse.getErrors());
        assertNull(aboutToStartOrSubmitCallbackResponse.getWarnings());
    }

    @Test
    public void testCallBackUrlMidEventThrowsError() {
        // Given

        // When
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            viewDraftOrdersController.callBackUrlMidEvent();

        // Then
        List<String> errors = aboutToStartOrSubmitCallbackResponse.getErrors();
        assertEquals(1, errors.size());
        assertEquals(READONLY_DRAFT_ORDERS_MESSAGE, errors.getFirst());

        assertNull(aboutToStartOrSubmitCallbackResponse.getData());
        assertNull(aboutToStartOrSubmitCallbackResponse.getWarnings());
    }

    @Test
    public void testCallBackUrlAboutToSubmitEvent() {
        // Given

        // When
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            viewDraftOrdersController.callBackUrlAboutToSubmitEvent();

        // Then
        List<String> errors = aboutToStartOrSubmitCallbackResponse.getErrors();
        assertEquals(1, errors.size());
        assertEquals(READONLY_DRAFT_ORDERS_MESSAGE, errors.getFirst());

        assertNull(aboutToStartOrSubmitCallbackResponse.getData());
        assertNull(aboutToStartOrSubmitCallbackResponse.getWarnings());
    }

    @Test
    public void testCallBackUrlSubmittedEvent() {
        // Given

        // When
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            viewDraftOrdersController.callBackUrlSubmittedEvent();

        // Then
        List<String> errors = aboutToStartOrSubmitCallbackResponse.getErrors();
        assertEquals(1, errors.size());
        assertEquals(READONLY_DRAFT_ORDERS_MESSAGE, errors.getFirst());

        assertNull(aboutToStartOrSubmitCallbackResponse.getData());
        assertNull(aboutToStartOrSubmitCallbackResponse.getWarnings());
    }
}
