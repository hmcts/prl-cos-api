package uk.gov.hmcts.reform.prl.controllers.closingcase;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.closingcase.ClosingCaseService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClosingCaseControllerTest {

    @InjectMocks
    ClosingCaseController closingCaseController;

    @Mock
    ClosingCaseService closingCaseService;

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Test
    public void testPrePopulateChildData() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        closingCaseController.prePopulateChildData(authToken, s2sToken, CallbackRequest.builder().build());
        verify(closingCaseService, times(1)).prePopulateChildData(Mockito.any(CallbackRequest.class));
    }

    @Test
    public void testPopulateSelectedChildWithFinalOutcome() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        closingCaseController.populateSelectedChildWithFinalOutcome(
            authToken,
            s2sToken,
            CallbackRequest.builder().build()
        );
        verify(closingCaseService, times(1)).populateSelectedChildWithFinalOutcome(Mockito.any(CallbackRequest.class));
    }

    @Test
    public void testValidateChildDetails() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        closingCaseController.validateChildDetails(authToken, s2sToken, CallbackRequest.builder().caseDetails(
            CaseDetails.builder().build()).build());
        verify(closingCaseService, times(1)).validateChildDetails(Mockito.any(CallbackRequest.class));
    }

    @Test
    public void testClosingCaseAboutToSubmit() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        closingCaseController.closingCaseAboutToSubmit(authToken, s2sToken, CallbackRequest.builder().build());
        verify(closingCaseService, times(1)).closingCaseForChildren(Mockito.any(CallbackRequest.class));
    }

    @Test
    public void testPrePopulateChildDataException() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        Assert.assertThrows(RuntimeException.class, () ->
            closingCaseController.prePopulateChildData(authToken, s2sToken, CallbackRequest.builder().build()));
    }

    @Test
    public void testPopulateSelectedChildWithFinalOutcomeException() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        Assert.assertThrows(RuntimeException.class, () -> closingCaseController.populateSelectedChildWithFinalOutcome(
            authToken,
            s2sToken,
            CallbackRequest.builder().build()
        ));
    }

    @Test
    public void testValidateChildDetailsException() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        Assert.assertThrows(RuntimeException.class,
                            () -> closingCaseController.validateChildDetails(authToken,
                                                                             s2sToken,
                                                                             CallbackRequest.builder().caseDetails(
                                                                                 CaseDetails.builder().build()).build()
                            ));
    }

    @Test
    public void testClosingCaseAboutToSubmitException() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        Assert.assertThrows(RuntimeException.class,
                            () -> closingCaseController.closingCaseAboutToSubmit(authToken,
                                                                                 s2sToken,
                                                                                 CallbackRequest.builder().build()
                            ));
    }
}
