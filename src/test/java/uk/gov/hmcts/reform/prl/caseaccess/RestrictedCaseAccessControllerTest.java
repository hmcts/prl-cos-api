package uk.gov.hmcts.reform.prl.caseaccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.controllers.caseaccess.RestrictedCaseAccessController;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.caseaccess.RestrictedCaseAccessService;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RestrictedCaseAccessControllerTest {

    public static final String AUTH_TOKEN = "auth-token";
    public static final String SERVICE_TOKEN = "service-token";
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private RestrictedCaseAccessService restrictedCaseAccessService;
    @InjectMocks
    private RestrictedCaseAccessController restrictedCaseAccessController;

    @Test
    public void testRestrictedCaseAccessAboutToSubmit() {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        restrictedCaseAccessController
            .restrictedCaseAccessAboutToSubmit(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        Mockito.verify(restrictedCaseAccessService,Mockito.times(1))
            .initiateUpdateCaseAccess(Mockito.any());
    }

    @Test(expected = RuntimeException.class)
    public void testRestrictedCaseAccessAboutToSubmitError() {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);
        restrictedCaseAccessController
            .restrictedCaseAccessAboutToSubmit(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());
    }

    @Test
    public void testRestrictedCaseAccessSubmitted() {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        restrictedCaseAccessController
                .restrictedCaseAccessSubmitted(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        Mockito.verify(restrictedCaseAccessService,Mockito.times(1))
                .changeCaseAccessRequestSubmitted(Mockito.any());
    }

    @Test(expected = RuntimeException.class)
    public void testRestrictedCaseAccessSubmittedError() {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);
        restrictedCaseAccessController
                .restrictedCaseAccessSubmitted(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());
    }

    @Test
    public void testChangeCaseAccess() throws JsonProcessingException {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        restrictedCaseAccessController
            .changeCaseAccess(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        Mockito.verify(restrictedCaseAccessService,Mockito.times(1))
            .changeCaseAccess(Mockito.any());
    }

    @Test(expected = RuntimeException.class)
    public void testChangeCaseAccessError() throws JsonProcessingException {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);
        restrictedCaseAccessController
            .changeCaseAccess(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());
    }

    @Test
    public void testRestrictedCaseAccessAboutToStartWithNoAccess() {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("errors", "errors");
        Mockito.when(restrictedCaseAccessService.retrieveAssignedUserRoles(Mockito.any())).thenReturn(caseDataUpdated);
        restrictedCaseAccessController
            .restrictedCaseAccessAboutToStart(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        Mockito.verify(restrictedCaseAccessService,Mockito.times(1))
            .retrieveAssignedUserRoles(Mockito.any());
    }

    @Test
    public void testRestrictedCaseAccessAboutToStartWithAccess() {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        Mockito.when(restrictedCaseAccessService.retrieveAssignedUserRoles(Mockito.any())).thenReturn(new HashMap<>());
        restrictedCaseAccessController
            .restrictedCaseAccessAboutToStart(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        Mockito.verify(restrictedCaseAccessService,Mockito.times(1))
            .retrieveAssignedUserRoles(Mockito.any());
    }

    @Test(expected = RuntimeException.class)
    public void testRestrictedCaseAccessAboutToStartError() throws JsonProcessingException {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);
        restrictedCaseAccessController
            .restrictedCaseAccessAboutToStart(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());
    }
}
