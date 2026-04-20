package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ExitAwaitingInformationService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_STATUS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ExitAwaitingInformationControllerTest {

    private static final String AUTH_TOKEN = "Bearer testAuthToken";
    private static final String S2S_TOKEN = "s2sTestToken";

    @InjectMocks
    private ExitAwaitingInformationController exitAwaitingInformationController;

    @Mock
    private ExitAwaitingInformationService exitAwaitingInformationService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private FeatureToggleService featureToggleService;

    private CallbackRequest callbackRequest;
    private Map<String, Object> caseData;

    @Before
    public void setUp() {
        caseData = new HashMap<>();
        caseData.put("testKey", "testValue");

        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(12345L)
                             .state(State.AWAITING_INFORMATION.getValue())
                             .data(caseData)
                             .build())
            .build();
    }

    @Test
    public void shouldSubmitExitAwaitingInformationSuccessfully() {
        Map<String, Object> updatedCaseData = new HashMap<>(caseData);
        updatedCaseData.put(STATE_FIELD, State.CASE_ISSUED.getValue());
        updatedCaseData.put(CASE_STATUS, CaseStatus.builder().state(State.CASE_ISSUED.getLabel()).build());

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(featureToggleService.isExitAwaitingInformationEnabled()).thenReturn(true);
        when(exitAwaitingInformationService.updateCase(callbackRequest)).thenReturn(updatedCaseData);

        AboutToStartOrSubmitCallbackResponse response = exitAwaitingInformationController
            .submitExitAwaitingInformation(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertNotNull(response);
        assertEquals(State.CASE_ISSUED.getValue(), response.getData().get(STATE_FIELD));
        verify(authorisationService, times(1)).isAuthorized(AUTH_TOKEN, S2S_TOKEN);
        verify(featureToggleService, times(1)).isExitAwaitingInformationEnabled();
        verify(exitAwaitingInformationService, times(1)).updateCase(callbackRequest);
    }

    @Test
    public void shouldPreserveReturnedCaseDataOnSubmit() {
        Map<String, Object> updatedCaseData = new HashMap<>(caseData);
        updatedCaseData.put("someField", "someValue");
        updatedCaseData.put(STATE_FIELD, State.SUBMITTED_PAID.getValue());
        updatedCaseData.put(CASE_STATUS, CaseStatus.builder().state(State.SUBMITTED_PAID.getLabel()).build());

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(featureToggleService.isExitAwaitingInformationEnabled()).thenReturn(true);
        when(exitAwaitingInformationService.updateCase(callbackRequest)).thenReturn(updatedCaseData);

        AboutToStartOrSubmitCallbackResponse response = exitAwaitingInformationController
            .submitExitAwaitingInformation(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertEquals("someValue", response.getData().get("someField"));
        assertEquals(State.SUBMITTED_PAID.getValue(), response.getData().get(STATE_FIELD));
    }

    @Test
    public void shouldThrowExceptionForSubmitWhenUnauthorized() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> exitAwaitingInformationController.submitExitAwaitingInformation(
                AUTH_TOKEN,
                S2S_TOKEN,
                callbackRequest
            )
        );

        assertEquals(INVALID_CLIENT, exception.getMessage());
        verify(authorisationService, times(1)).isAuthorized(AUTH_TOKEN, S2S_TOKEN);
        verify(featureToggleService, never()).isExitAwaitingInformationEnabled();
        verify(exitAwaitingInformationService, never()).updateCase(any());
    }

    @Test
    public void shouldThrowExceptionForSubmitWhenFeatureDisabled() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(featureToggleService.isExitAwaitingInformationEnabled()).thenReturn(false);

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> exitAwaitingInformationController.submitExitAwaitingInformation(
                AUTH_TOKEN,
                S2S_TOKEN,
                callbackRequest
            )
        );

        assertEquals(INVALID_CLIENT, exception.getMessage());
        verify(authorisationService, times(1)).isAuthorized(AUTH_TOKEN, S2S_TOKEN);
        verify(featureToggleService, times(1)).isExitAwaitingInformationEnabled();
        verify(exitAwaitingInformationService, never()).updateCase(any());
    }
}
