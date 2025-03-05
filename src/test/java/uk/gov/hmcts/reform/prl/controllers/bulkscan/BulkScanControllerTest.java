package uk.gov.hmcts.reform.prl.controllers.bulkscan;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@PropertySource(value = "classpath:application.yaml")
public class BulkScanControllerTest {

    @InjectMocks
    private BulkScanController bulkScanController;

    @Mock
    AllTabServiceImpl allTabsService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private PaymentRequestService paymentRequestService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Test
    public void testExceptionForBulkScanCaseSubmission() {
        Map<String, Object> caseDetails = new HashMap<>();

        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseDetails).build()).build();

        assertExpectedException(() -> {
            bulkScanController.bulkScanCaseSubmission(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testBulkScanCaseSubmission() throws Exception {
        when(paymentRequestService.createServiceRequestFromCcdCallack(Mockito.any(),Mockito.any()))
            .thenReturn(PaymentServiceResponse.builder().serviceRequestReference("1234").build());
        when(allTabsService.updateAllTabsIncludingConfTab(Mockito.any()))
            .thenReturn(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().build());
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = bulkScanController
            .bulkScanCaseSubmission(authToken, s2sToken, CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                 .data(new HashMap<>()).build()).build());
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    public void testExceptionForBulkScanSubmittedCallBack() {
        Map<String, Object> caseDetails = new HashMap<>();

        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseDetails).build()).build();

        assertExpectedException(() -> {
            bulkScanController.bulkScanHandleSubmitted(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testBulkScanSubmittedCAllBack() throws Exception {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = bulkScanController
            .bulkScanHandleSubmitted(authToken, s2sToken, CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                 .data(new HashMap<>()).build()).build());
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
