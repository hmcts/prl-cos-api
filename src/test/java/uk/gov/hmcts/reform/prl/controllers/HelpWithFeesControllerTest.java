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

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";

    @InjectMocks
    private HelpWithFeesController helpWithFeesController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private HelpWithFeesService helpWithFeesService;

    private CaseDetails caseDetails;

    private CallbackRequest callbackRequest;

    @Before
    public void setup() {
        caseDetails = CaseDetails.builder()
            .id(123L)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    @Test
    public void test_HelpWithFeesAboutToStart() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        helpWithFeesController.handleAboutToStart(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verify(helpWithFeesService, times(1)).handleAboutToStart(caseDetails);
    }

    @Test(expected = RuntimeException.class)
    public void test_HelpWithFeesAboutToStartThrowsException() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);
        helpWithFeesController.handleAboutToStart(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verifyNoInteractions(helpWithFeesService);
    }

    @Test
    public void test_HelpWithFeesAboutToSubmit() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        helpWithFeesController.handleAboutToSubmit(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verify(helpWithFeesService, times(1)).setCaseStatus(caseDetails);
    }

    @Test(expected = RuntimeException.class)
    public void test_HelpWithFeesAboutToSubmitThrowsException() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);
        helpWithFeesController.handleAboutToSubmit(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verifyNoInteractions(helpWithFeesService);
    }

    @Test
    public void test_HelpWithFeesHandleSubmitted() throws Exception {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        helpWithFeesController.handleSubmitted(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verify(helpWithFeesService, times(1)).handleSubmitted();
    }

    @Test(expected = RuntimeException.class)
    public void test_HelpWithFeesHandleSubmittedThrowsException() throws Exception {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);
        helpWithFeesController.handleSubmitted(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verifyNoInteractions(helpWithFeesService);
    }

}
