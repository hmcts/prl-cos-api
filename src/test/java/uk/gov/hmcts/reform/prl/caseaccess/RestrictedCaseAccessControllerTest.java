package uk.gov.hmcts.reform.prl.caseaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.controllers.caseaccess.RestrictedCaseAccessController;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.caseaccess.RestrictedCaseAccessService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class RestrictedCaseAccessControllerTest {

    public static final String AUTH_TOKEN = "auth-token";
    public static final String SERVICE_TOKEN = "service-token";
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private RestrictedCaseAccessService restrictedCaseAccessService;
    @InjectMocks
    private RestrictedCaseAccessController restrictedCaseAccessController;

    @Test
    void testRestrictedCaseAccessAboutToSubmit() {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        restrictedCaseAccessController
            .restrictedCaseAccessAboutToSubmit(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        Mockito.verify(restrictedCaseAccessService,Mockito.times(1))
            .initiateUpdateCaseAccess(Mockito.any());
    }

    @Test
    void testRestrictedCaseAccessAboutToSubmitError() {
        assertThrows(RuntimeException.class, () -> {
            Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);
            restrictedCaseAccessController
            .restrictedCaseAccessAboutToSubmit(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());
        });
    }

    @Test
    void testRestrictedCaseAccessSubmitted() {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        restrictedCaseAccessController
                .restrictedCaseAccessSubmitted(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        Mockito.verify(restrictedCaseAccessService,Mockito.times(1))
                .changeCaseAccessRequestSubmitted(Mockito.any());
    }

    @Test
    void testRestrictedCaseAccessSubmittedError() {
        assertThrows(RuntimeException.class, () -> {
            Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);
            restrictedCaseAccessController
            .restrictedCaseAccessSubmitted(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());
        });
    }

    @Test
    void testChangeCaseAccess() {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        restrictedCaseAccessController
            .changeCaseAccess(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        Mockito.verify(restrictedCaseAccessService,Mockito.times(1))
            .changeCaseAccess(Mockito.any());
    }

    @Test
    void testChangeCaseAccessError() {
        assertThrows(RuntimeException.class, () -> {
            Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);
            restrictedCaseAccessController
            .changeCaseAccess(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());
        });
    }

    @Test
    void testRestrictedCaseAccessAboutToStartWithNoAccess() {

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
    void testRestrictedCaseAccessAboutToStartWithAccess() {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        Mockito.when(restrictedCaseAccessService.retrieveAssignedUserRoles(Mockito.any())).thenReturn(new HashMap<>());
        restrictedCaseAccessController
            .restrictedCaseAccessAboutToStart(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        Mockito.verify(restrictedCaseAccessService,Mockito.times(1))
            .retrieveAssignedUserRoles(Mockito.any());
    }

    @Test
    void testRestrictedCaseAccessAboutToStartError() {
        assertThrows(RuntimeException.class, () -> {
            Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);
            restrictedCaseAccessController
            .restrictedCaseAccessAboutToStart(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());
        });
    }
}
