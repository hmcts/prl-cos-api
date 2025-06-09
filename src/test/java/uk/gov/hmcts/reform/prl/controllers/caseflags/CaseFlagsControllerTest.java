package uk.gov.hmcts.reform.prl.controllers.caseflags;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsService;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsWaService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseFlagsControllerTest {

    public static final String AUTH_TOKEN = "auth-token";
    public static final String SERVICE_TOKEN = "service-token";
    public static final String CLIENT_CONTEXT = "client-context";
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private CaseFlagsWaService caseFlagsWaService;
    @Mock
    private CaseFlagsService caseFlagsService;
    @InjectMocks
    private CaseFlagsController caseFlagsController;

    @Test
    public void tesSetUpWaTaskForCaseFlags2() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        caseFlagsController
            .setUpWaTaskForCaseFlags2(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        verify(caseFlagsWaService, times(1))
            .setUpWaTaskForCaseFlagsEventHandler(Mockito.any(),Mockito.any());
    }

    @Test(expected = RuntimeException.class)
    public void tesSetUpWaTaskForCaseFlags2WhenAuthorisationFails() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);
        caseFlagsController
            .setUpWaTaskForCaseFlags2(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());
    }

    @Test
    public void testReviewLangAndSmAboutToStart() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .data(Map.of())
                    .build())
            .build();
        caseFlagsController
            .handleAboutToStart(AUTH_TOKEN, SERVICE_TOKEN, CLIENT_CONTEXT,callbackRequest);
        verify(caseFlagsService, times(1)).prepareSelectedReviewLangAndSmReq(Map.of(), CLIENT_CONTEXT);
        verify(authorisationService, times(1)).isAuthorized(AUTH_TOKEN, SERVICE_TOKEN);
    }

    @Test
    public void testReviewLangAndSmAboutToStartWhenAuthorisationFails() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> {
            caseFlagsController
                .handleAboutToStart(AUTH_TOKEN, SERVICE_TOKEN, CLIENT_CONTEXT,CallbackRequest.builder().build());
        });
        verify(caseFlagsService, never()).prepareSelectedReviewLangAndSmReq(Map.of(), CLIENT_CONTEXT);
    }


    @Test
    public void testHandleMidEventWithErrors() {
        List<String> errors = List.of("Please select");
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .data(Map.of())
                    .build())
            .build();
        when(caseFlagsService.isLangAndSmReqReviewed(Map.of()))
            .thenReturn(errors);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = caseFlagsController
            .handleMidEvent(AUTH_TOKEN, callbackRequest);
        assertThat(aboutToStartOrSubmitCallbackResponse.getErrors()).containsAll(errors);
        verify(caseFlagsService, times(1)).isLangAndSmReqReviewed(Map.of());
    }

    @Test
    public void testHandleAboutToSubmitEventWithErrors() {
        List<String> errors = List.of("Please select");
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(
                CaseDetails.builder()
                    .data(Map.of())
                    .build()
            )
            .caseDetails(
                CaseDetails.builder()
                    .data(Map.of())
                    .build())
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        when(caseFlagsService.validateNewCaseFlagStatus(Map.of(), Map.of()))
            .thenReturn(errors);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = caseFlagsController
            .handleAboutToSubmit(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest);
        assertThat(aboutToStartOrSubmitCallbackResponse.getErrors()).containsAll(errors);
        verify(caseFlagsService, times(1)).validateNewCaseFlagStatus(Map.of(), Map.of());
    }

    @Test
    public void testHandleAboutToSubmitEventInvalidClientId() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .data(Map.of())
                    .build())
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> {
            caseFlagsController
                .handleAboutToSubmit(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest);
        });
        verify(caseFlagsService, never()).isLangAndSmReqReviewed(Map.of());
    }
}

