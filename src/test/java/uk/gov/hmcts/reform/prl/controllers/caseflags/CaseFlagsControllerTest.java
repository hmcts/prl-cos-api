package uk.gov.hmcts.reform.prl.controllers.caseflags;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsService;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsWaService;

import java.util.Map;

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
}
