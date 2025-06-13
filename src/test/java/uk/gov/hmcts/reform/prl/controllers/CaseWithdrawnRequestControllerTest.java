package uk.gov.hmcts.reform.prl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CaseWithdrawnRequestService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseWithdrawnRequestControllerTest {

    @InjectMocks
    CaseWithdrawnRequestController caseWithdrawnRequestController;

    @Mock
    CaseWithdrawnRequestService caseWithdrawnRequestService;

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;
    CallbackRequest callbackRequest;

    @BeforeEach
    void setup() {
        caseDataMap = new HashMap<>();
        caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    @Test
    void testAboutToSubmitCaseCreation() throws Exception {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        caseWithdrawnRequestController.caseWithdrawnEmailNotificationWhenSubmitted(
            authToken,
            s2sToken,
            callbackRequest
        );
        verify(caseWithdrawnRequestService, times(1)).caseWithdrawnEmailNotification(
            Mockito.any(CallbackRequest.class),
            Mockito.anyString()
        );
    }

    @Test
    void testExceptionForCaseWithdrawnEmailNotificationWhenSubmitted() throws Exception {

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> {
                caseWithdrawnRequestController.caseWithdrawnEmailNotificationWhenSubmitted(
                    authToken,
                    s2sToken,
                    callbackRequest
                );
            }
        );

        assertEquals("Invalid Client", ex.getMessage());
    }
}
