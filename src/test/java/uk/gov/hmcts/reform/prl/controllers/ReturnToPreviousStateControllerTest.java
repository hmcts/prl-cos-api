package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReturnToPreviousStateControllerTest {

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private AllTabServiceImpl tabService;

    @Mock
    private EventService eventService;

    private ReturnToPreviousStateController controller;

    private static final String AUTH_TOKEN = "Bearer TestAuthToken";
    private static final String S2S_TOKEN = "s2s TestToken";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ReturnToPreviousStateController(
            new ObjectMapper(),
            eventService,
            authorisationService,
            tabService
        );
    }

    @Test
    void handleAboutToSubmit_whenAuthorized_returnsNewStateAndData() throws Exception {
        // Arrange
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("foo", "bar");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(dataMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response =
            controller.handleAboutToSubmit(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertEquals(
            State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue(),
            response.getState(),
            "State should be updated to PREPARE_FOR_HEARING_CONDUCT_HEARING"
        );
        assertSame(
            dataMap,
            response.getData(),
            "Returned data map should be the same instance passed in"
        );
    }

    @Test
    void handleAboutToSubmit_whenNotAuthorized_throwsRuntimeException() {
        Map<String, Object> dataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(dataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> controller.handleAboutToSubmit(AUTH_TOKEN, S2S_TOKEN, callbackRequest),
            "Expected a RuntimeException when unauthorized"
        );
        assertEquals(
            PrlAppsConstants.INVALID_CLIENT,
            ex.getMessage(),
            "Exception message should be INVALID_CLIENT"
        );
    }

    @Test
    void shouldCallUpdateAllTabsIncludingConfTab() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        controller.handleSubmitted(AUTH_TOKEN, callbackRequest);

        verify(tabService, times(1)).updateAllTabsIncludingConfTab("12345");
        verifyNoMoreInteractions(tabService);
    }

    @Test
    void handleAboutToSubmit_overwritesExistingState() throws Exception {
        // Arrange
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("foo", "bar");

        // simulate an existing state on the case
        CaseDetails caseDetails = CaseDetails.builder()
            .id(999L)
            .data(dataMap)
            .state(State.DECISION_OUTCOME.getValue())
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);

        // Act
        AboutToStartOrSubmitCallbackResponse response =
            controller.handleAboutToSubmit(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        // Assert
        // 1) It uses the new state, not the old one
        assertEquals(
            State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue(),
            response.getState(),
            "Controller should replace any existing state with PREPARE_FOR_HEARING_CONDUCT_HEARING"
        );
        assertNotEquals(
            State.DECISION_OUTCOME.getValue(),
            response.getState(),
            "Old state must not persist"
        );
        // 2) The payload data remains the same
        assertSame(
            dataMap,
            response.getData(),
            "Data map should be passed through untouched"
        );
    }

}
