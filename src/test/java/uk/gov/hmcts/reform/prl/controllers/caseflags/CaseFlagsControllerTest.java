package uk.gov.hmcts.reform.prl.controllers.caseflags;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsWaService;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseFlagsControllerTest {

    public static final String AUTH_TOKEN = "auth-token";
    public static final String SERVICE_TOKEN = "service-token";
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private CaseFlagsWaService caseFlagsWaService;
    @InjectMocks
    private CaseFlagsController caseFlagsController;

    @Test
    public void tesSetUpWaTaskForCaseFlags2() {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        caseFlagsController
            .setUpWaTaskForCaseFlags2(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        Mockito.verify(caseFlagsWaService,Mockito.times(1))
            .setUpWaTaskForCaseFlagsEventHandler(Mockito.any(),Mockito.any());
    }

    @Test(expected = RuntimeException.class)
    public void tesSetUpWaTaskForCaseFlags2WhenAuthorisationFails() {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);
        caseFlagsController
            .setUpWaTaskForCaseFlags2(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());
    }
}
