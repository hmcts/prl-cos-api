package uk.gov.hmcts.reform.prl.controllers.reopenclosedcases;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.reopenclosedcases.ReopenClosedCasesService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ReopenClosedCasesControllerTest {

    @InjectMocks
    ReopenClosedCasesController reopenClosedCasesController;

    @Mock
    ReopenClosedCasesService reopenClosedCasesService;

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";


    @Test
    public void testClosingCaseAboutToSubmit() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        reopenClosedCasesController.reopenClosedCases(authToken, s2sToken, CallbackRequest.builder().build());
        verify(reopenClosedCasesService, times(1)).reopenClosedCases(Mockito.any(CallbackRequest.class));
    }


    @Test
    public void testClosingCaseAboutToSubmitException() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        Assert.assertThrows(RuntimeException.class,
                            () -> reopenClosedCasesController.reopenClosedCases(authToken,
                                                                                 s2sToken,
                                                                                 CallbackRequest.builder().build()
                            ));
    }
}
