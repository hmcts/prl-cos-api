package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.caseinitiation.CaseInitiationService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseInitiationControllerTest {

    @InjectMocks
    private CaseInitiationController caseInitiationController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CaseInitiationService caseInitiationService;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";

    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;

    CaseData caseData;
    CallbackRequest callbackRequest;

    @BeforeEach
    void setUp() {
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
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
    }

    @Test
    void testHandleSubmitted() {
        caseInitiationController.handleSubmitted(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verify(caseInitiationService, times(1)).handleCaseInitiation(
            AUTH_TOKEN,
            callbackRequest
        );
    }

    @Test
    void testExceptionForHandleSubmitted() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            caseInitiationController.handleSubmitted(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        });
        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testHandlePopulateCourtList() {
        caseInitiationController.populateCourtList(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verify(caseInitiationService, times(1)).prePopulateCourtDetails(
            AUTH_TOKEN,
            new HashMap<>()
        );
    }
}

