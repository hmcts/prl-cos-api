package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@PropertySource(value = "classpath:application.yaml")
@RunWith(SpringRunner.class)
public class FeeAndPayServiceRequestControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private FeeAndPayServiceRequestController feeAndPayServiceRequestController;

    @Mock
    private PaymentRequestService paymentRequestService;

    @Mock
    private FeeService feesService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private FeeResponse feeResponse;

    @Mock
    private PaymentServiceResponse paymentServiceResponse;
    @Mock
    private SolicitorEmailService solicitorEmailService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private EventService eventPublisher;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Before
    public void setUp() {

        paymentServiceResponse = PaymentServiceResponse.builder()
            .serviceRequestReference("2021-1638188893038")
            .build();

        feeResponse = FeeResponse.builder()
            .code("FEE0325")
            .build();
    }

    @Test
    public void testFeeServiceFeeCodeDetails() throws Exception {

        FeeType feeType = null;

        CallbackRequest callbackRequest = CallbackRequest.builder().build();

        when(paymentRequestService.createServiceRequest(callbackRequest,authToken)).thenReturn(paymentServiceResponse);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(feesService.fetchFeeDetails(feeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        verifyNoMoreInteractions(feesService);

    }

    @Test
    public void testCcdSubmitted() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId("123")
                             .state("PENDING").caseData(CaseData.builder()
                                                            .applicantSolicitorEmailAddress("hello@gmail.com")
                                                            .build()).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        ResponseEntity response = feeAndPayServiceRequestController.ccdSubmitted(authToken, s2sToken, callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void testExceptionForCcdSubmitted() throws Exception {

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId("123")
                             .state("PENDING").caseData(CaseData.builder()
                                                            .applicantSolicitorEmailAddress("hello@gmail.com")
                                                            .build()).build()).build();

        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            feeAndPayServiceRequestController.ccdSubmitted(authToken, s2sToken,
                                                           callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

}
