package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.HelpWithFeesService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HelpWithFeesControllerTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @InjectMocks
    private HelpWithFeesController helpWithFeesController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private HelpWithFeesService helpWithFeesService;

    private CaseDetails caseDetails;

    private CallbackRequest callbackRequest;

    @Before
    public void setup(){
        caseDetails = CaseDetails.builder()
            .id(123L)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    @Test
    public void test_HelpWithFeesAboutToStart() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        helpWithFeesController.handleAboutToStart(authToken, s2sToken, callbackRequest);
        verify(helpWithFeesService, times(1)).handleAboutToStart(authToken,caseDetails);
    }

    @Test(expected = RuntimeException.class)
    public void test_HelpWithFeesAboutToStartThrowsException() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        helpWithFeesController.handleAboutToStart( authToken, s2sToken, callbackRequest);
        verifyNoInteractions(helpWithFeesService);
    }

    @Test
    public void test_HelpWithFeesHandleSubmitted() throws Exception {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        helpWithFeesController.handleSubmitted(authToken, s2sToken, callbackRequest);
        verify(helpWithFeesService, times(1)).handleSubmitted();
    }

    @Test(expected = RuntimeException.class)
    public void test_HelpWithFeesHandleSubmittedThrowsException() throws Exception {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        helpWithFeesController.handleSubmitted(authToken, s2sToken, callbackRequest);
        verifyNoInteractions(helpWithFeesService);
    }

}
