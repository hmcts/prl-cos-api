package uk.gov.hmcts.reform.prl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.HelpWithFeesService;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HelpWithFeesControllerTest {

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

    @BeforeEach
    void setup() {
        caseDetails = CaseDetails.builder()
            .id(123L)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    @Test
    void test_HelpWithFeesAboutToStart() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        helpWithFeesController.handleAboutToStart(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verify(helpWithFeesService, times(1)).handleAboutToStart(caseDetails);
    }

    @Test
    void test_HelpWithFeesAboutToStartThrowsException() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            helpWithFeesController.handleAboutToStart(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        });

        verifyNoInteractions(helpWithFeesService);
    }

    @Test
    void test_HelpWithFeesAboutToSubmit() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        helpWithFeesController.handleAboutToSubmit(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verify(helpWithFeesService, times(1)).setCaseStatus(callbackRequest, AUTH_TOKEN);
    }

    @Test
    void test_HelpWithFeesAboutToSubmitThrowsException() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            helpWithFeesController.handleAboutToSubmit(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        });

        verifyNoInteractions(helpWithFeesService);
    }

    @Test
    void test_HelpWithFeesHandleSubmitted() throws Exception {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        helpWithFeesController.handleSubmitted(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verify(helpWithFeesService, times(1)).handleSubmitted();
    }

    @Test
    void test_HelpWithFeesHandleSubmittedThrowsException() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            helpWithFeesController.handleSubmitted(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        });

        verifyNoInteractions(helpWithFeesService);
    }

    @Test
    void test_populateHwfDynamicData() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        helpWithFeesController.populateHwfDynamicData(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verify(helpWithFeesService, times(1)).populateHwfDynamicData(callbackRequest.getCaseDetails());
    }

    @Test
    void test_populateHwfDynamicDataThrowsException() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            helpWithFeesController.populateHwfDynamicData(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        });

        verifyNoInteractions(helpWithFeesService);
    }

    @Test
    void test_checkForManagerApproval_1() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        helpWithFeesController.checkForManagerApproval(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verify(helpWithFeesService, times(1)).checkForManagerApproval(callbackRequest.getCaseDetails());
    }

    @Test
    void test_checkForManagerApproval_2() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(helpWithFeesService.checkForManagerApproval(callbackRequest.getCaseDetails())).thenReturn(Arrays.asList("test"));
        helpWithFeesController.checkForManagerApproval(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verify(helpWithFeesService, times(1)).checkForManagerApproval(callbackRequest.getCaseDetails());
    }

    @Test
    void test_checkForManagerApprovalThrowsException() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            helpWithFeesController.checkForManagerApproval(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        });

        verifyNoInteractions(helpWithFeesService);
    }
}
