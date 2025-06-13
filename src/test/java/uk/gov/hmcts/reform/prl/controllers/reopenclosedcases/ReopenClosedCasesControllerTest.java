package uk.gov.hmcts.reform.prl.controllers.reopenclosedcases;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.reopenclosedcases.ReopenClosedCasesService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ReopenClosedCasesControllerTest {

    @InjectMocks
    ReopenClosedCasesController reopenClosedCasesController;

    @Mock
    ReopenClosedCasesService reopenClosedCasesService;

    @Mock
    private AuthorisationService authorisationService;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S_2_S_TOKEN = "s2s AuthToken";


    @Test
    void testClosingCaseAboutToSubmit() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        reopenClosedCasesController.reopenClosedCases(AUTH_TOKEN, S_2_S_TOKEN, CallbackRequest.builder().build());
        verify(reopenClosedCasesService, times(1)).reopenClosedCases(Mockito.any(CallbackRequest.class));
    }


    @Test
    void testClosingCaseAboutToSubmitException() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        assertThrows(RuntimeException.class,
                            () -> reopenClosedCasesController.reopenClosedCases(
                                AUTH_TOKEN,
                                S_2_S_TOKEN,
                                callbackRequest
                            ));
    }
}
