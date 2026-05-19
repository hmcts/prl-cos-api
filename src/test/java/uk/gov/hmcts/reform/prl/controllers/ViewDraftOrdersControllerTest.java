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
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ViewDraftOrdersService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    private Element<DraftOrder> mockDraftOrderElement;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Test
    public void testCallBackURLAboutToStartEventFailsAuthorisation() {
        // Given
        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);

        // When
        InvalidClientException exception = assertThrows(InvalidClientException.class, () -> {
            viewDraftOrdersController.callBackURLAboutToStartEvent(authToken, s2sToken, callbackRequest);
        });

        // Then
        assertEquals(INVALID_CLIENT, exception.getMessage());
    }

    @Test
    public void testCallBackURLAboutToStartEvent() {
        // Given
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1L)
            .data(Collections.emptyMap()).build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails).build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        when(viewDraftOrdersService.getDraftOrdersForUser(callbackRequest.getCaseDetails(), authToken))
            .thenReturn(List.of(mockDraftOrderElement));

        // When
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            viewDraftOrdersController.callBackURLAboutToStartEvent(authToken, s2sToken, callbackRequest);

        // Then
        Map<String, Object> data = aboutToStartOrSubmitCallbackResponse.getData();
        assertEquals(1, data.size());

        Object viewFilteredDraftOrdersObj = data.get("viewFilteredDraftOrders");
        assertInstanceOf(List.class, viewFilteredDraftOrdersObj);

        List<Element<DraftOrder>> viewFilteredDraftOrdersList = (List) viewFilteredDraftOrdersObj;
        assertEquals(1, viewFilteredDraftOrdersList.size());
        assertEquals(mockDraftOrderElement, viewFilteredDraftOrdersList.getFirst());

        assertNull(aboutToStartOrSubmitCallbackResponse.getErrors());
        assertNull(aboutToStartOrSubmitCallbackResponse.getWarnings());
    }

    @Test
    public void testCallBackURLMidEventThrowsError() {
        // Given

        // When
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            viewDraftOrdersController.callBackURLMidEvent();

        // Then
        List<String> errors = aboutToStartOrSubmitCallbackResponse.getErrors();
        assertEquals(1, errors.size());
        assertEquals(READONLY_DRAFT_ORDERS_MESSAGE, errors.getFirst());

        assertNull(aboutToStartOrSubmitCallbackResponse.getData());
        assertNull(aboutToStartOrSubmitCallbackResponse.getWarnings());
    }

    @Test
    public void testCallBackURLAboutToSubmitEvent() {
        // Given

        // When
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            viewDraftOrdersController.callBackURLAboutToSubmitEvent();

        // Then
        List<String> errors = aboutToStartOrSubmitCallbackResponse.getErrors();
        assertEquals(1, errors.size());
        assertEquals(READONLY_DRAFT_ORDERS_MESSAGE, errors.getFirst());

        assertNull(aboutToStartOrSubmitCallbackResponse.getData());
        assertNull(aboutToStartOrSubmitCallbackResponse.getWarnings());
    }

    @Test
    public void testCallBackURLSubmittedEvent() {
        // Given

        // When
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            viewDraftOrdersController.callBackURLSubmittedEvent();

        // Then
        List<String> errors = aboutToStartOrSubmitCallbackResponse.getErrors();
        assertEquals(1, errors.size());
        assertEquals(READONLY_DRAFT_ORDERS_MESSAGE, errors.getFirst());

        assertNull(aboutToStartOrSubmitCallbackResponse.getData());
        assertNull(aboutToStartOrSubmitCallbackResponse.getWarnings());
    }
}
