package uk.gov.hmcts.reform.prl.controllers.closingcase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.closingcase.ClosingCaseService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClosingCaseControllerTest {

    @InjectMocks
    ClosingCaseController closingCaseController;

    @Mock
    ClosingCaseService closingCaseService;

    @Mock
    private AuthorisationService authorisationService;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S_2_S_TOKEN = "s2s AuthToken";

    @Test
    void testPrePopulateChildData() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        closingCaseController.prePopulateChildData(AUTH_TOKEN, S_2_S_TOKEN, CallbackRequest.builder().build());
        verify(closingCaseService, times(1)).prePopulateChildData(Mockito.any(CallbackRequest.class));
    }

    @Test
    void testPopulateSelectedChildWithFinalOutcome() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        closingCaseController.populateSelectedChildWithFinalOutcome(
            AUTH_TOKEN,
            S_2_S_TOKEN,
            CallbackRequest.builder().build()
        );
        verify(closingCaseService, times(1)).populateSelectedChildWithFinalOutcome(Mockito.any(CallbackRequest.class));
    }

    @Test
    void testValidateChildDetails() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        closingCaseController.validateChildDetails(AUTH_TOKEN, S_2_S_TOKEN, CallbackRequest.builder().caseDetails(
            CaseDetails.builder().build()).build());
        verify(closingCaseService, times(1)).validateChildDetails(Mockito.any(CallbackRequest.class));
    }

    @Test
    void testClosingCaseAboutToSubmit() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        closingCaseController.closingCaseAboutToSubmit(AUTH_TOKEN, S_2_S_TOKEN, CallbackRequest.builder().build());
        verify(closingCaseService, times(1)).closingCaseForChildren(Mockito.any(CallbackRequest.class));
    }

    @Test
    void testPrePopulateChildDataException() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        assertThrows(RuntimeException.class, () ->
            closingCaseController.prePopulateChildData(AUTH_TOKEN, S_2_S_TOKEN, callbackRequest));
    }

    @Test
    void testPopulateSelectedChildWithFinalOutcomeException() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        assertThrows(RuntimeException.class, () -> closingCaseController.populateSelectedChildWithFinalOutcome(
            AUTH_TOKEN,
            S_2_S_TOKEN,
            callbackRequest
        ));
    }

    @Test
    void testValidateChildDetailsException() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(
            CaseDetails.builder().build()).build();
        assertThrows(RuntimeException.class,
                            () -> closingCaseController.validateChildDetails(
                                AUTH_TOKEN,
                                S_2_S_TOKEN,
                                callbackRequest
                            ));
    }

    @Test
    void testClosingCaseAboutToSubmitException() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        assertThrows(RuntimeException.class,
                            () -> closingCaseController.closingCaseAboutToSubmit(
                                AUTH_TOKEN,
                                S_2_S_TOKEN,
                                callbackRequest
                            ));
    }
}
