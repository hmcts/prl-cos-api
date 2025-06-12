package uk.gov.hmcts.reform.prl.caseaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.controllers.caseaccess.RestrictedCaseAccessController;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.caseaccess.RestrictedCaseAccessService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RestrictedCaseAccessControllerTest {

    public static final String AUTH_TOKEN = "auth-token";
    public static final String SERVICE_TOKEN = "service-token";
    public static final String INVALID_CLIENT = "Invalid Client";
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private RestrictedCaseAccessService restrictedCaseAccessService;
    @InjectMocks
    private RestrictedCaseAccessController restrictedCaseAccessController;

    @Test
    void testRestrictedCaseAccessAboutToSubmit() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = restrictedCaseAccessController
            .restrictedCaseAccessAboutToSubmit(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        assertEquals(AboutToStartOrSubmitCallbackResponse.class, response.getClass());

        verify(restrictedCaseAccessService,Mockito.times(1))
            .initiateUpdateCaseAccess(Mockito.any());

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void testRestrictedCaseAccessAboutToSubmitError() {

        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            restrictedCaseAccessController
                .restrictedCaseAccessAboutToSubmit(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest)
        );

        assertEquals(INVALID_CLIENT, exception.getMessage());

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void testRestrictedCaseAccessSubmitted() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);

        ResponseEntity<SubmittedCallbackResponse> response = restrictedCaseAccessController
            .restrictedCaseAccessSubmitted(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        assertEquals(200, response.getStatusCode().value());
        verify(restrictedCaseAccessService,Mockito.times(1))
                .changeCaseAccessRequestSubmitted(Mockito.any());
    }

    @Test
    void testRestrictedCaseAccessSubmittedError() {

        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            restrictedCaseAccessController
                .restrictedCaseAccessSubmitted(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest)
        );

        assertEquals(INVALID_CLIENT, exception.getMessage());

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void testChangeCaseAccess() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);

        uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse response = restrictedCaseAccessController
           .changeCaseAccess(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

       assertEquals(AboutToStartOrSubmitCallbackResponse.class, response.getClass());

        verify(restrictedCaseAccessService,Mockito.times(1))
            .changeCaseAccess(Mockito.any());

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_TOKEN);
    }

    @Test
    public void testChangeCaseAccessError() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            restrictedCaseAccessController
                .changeCaseAccess(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build())
        );

        assertEquals(INVALID_CLIENT, exception.getMessage());

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_TOKEN);
    }

    @Test
    public void testRestrictedCaseAccessAboutToStartWithNoAccess() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("errors", "errors");
        when(restrictedCaseAccessService.retrieveAssignedUserRoles(Mockito.any())).thenReturn(caseDataUpdated);
        restrictedCaseAccessController
            .restrictedCaseAccessAboutToStart(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        verify(restrictedCaseAccessService,Mockito.times(1))
            .retrieveAssignedUserRoles(Mockito.any());

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_TOKEN);
    }

    @Test
    public void testRestrictedCaseAccessAboutToStartWithAccess() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        when(restrictedCaseAccessService.retrieveAssignedUserRoles(Mockito.any())).thenReturn(new HashMap<>());
        restrictedCaseAccessController
            .restrictedCaseAccessAboutToStart(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        Mockito.verify(restrictedCaseAccessService,Mockito.times(1))
            .retrieveAssignedUserRoles(Mockito.any());
    }

    @Test
    public void testRestrictedCaseAccessAboutToStartError() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            restrictedCaseAccessController
                .restrictedCaseAccessAboutToStart(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build())
        );

        assertEquals(INVALID_CLIENT, exception.getMessage());

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_TOKEN);
    }
}
