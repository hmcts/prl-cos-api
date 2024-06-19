package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.FeeAndPayServiceRequestService;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.class)
public class FeeAndPayServiceRequestControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private FeeAndPayServiceRequestController feeAndPayServiceRequestController;

    @Mock
    private PaymentRequestService paymentRequestService;

    @Mock
    private AuthorisationService authorisationService;

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
    private FeeAndPayServiceRequestService feeAndPayServiceRequestService;

    @Mock
    private EventService eventPublisher;

    List<String> errorList;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";
    public static final String HWF_SUPPRESSION_ERROR_MESSAGE =
        "Help with Fees is not yet available in Family Private Law digital service. Select 'No' to continue with your application";

    @Before
    public void setUp() {
        errorList = new ArrayList<>();
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

        verifyNoMoreInteractions(feesService);
    }

    @Test
    public void testCcdSubmittedHelpWithFeesYes() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId("123")
                             .state("PENDING").caseData(CaseData.builder()
                                                            .applicantSolicitorEmailAddress("hello@gmail.com")
                                                            .helpWithFees(YesOrNo.Yes)
                                                            .build()).build()).build();
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        ResponseEntity response = feeAndPayServiceRequestController.ccdSubmitted(authToken, s2sToken, callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void testCcdSubmittedHelpWithhFeesNo() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId("123")
                             .state("PENDING").caseData(CaseData.builder()
                                                            .applicantSolicitorEmailAddress("hello@gmail.com")
                                                            .helpWithFees(YesOrNo.No)
                                                            .build()).build()).build();
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        ResponseEntity response = feeAndPayServiceRequestController.ccdSubmitted(authToken, s2sToken, callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void testHelpWithFeesValidatorNotValidForSubmitAndPay() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId("123")
                             .state("PENDING").caseData(CaseData.builder()
                                                            .applicantSolicitorEmailAddress("hello@gmail.com")
                                                            .helpWithFees(YesOrNo.Yes)
                                                            .helpWithFeesNumber("$$$$$$")
                                                            .build()).build())
            .eventId(Event.SUBMIT_AND_PAY.getId())
            .build();
        when(feeAndPayServiceRequestService.validateSuppressedHelpWithFeesCheck(callbackRequest)).thenCallRealMethod();
        Assert.assertEquals(0,
            feeAndPayServiceRequestController.helpWithFeesValidator(authToken, callbackRequest).getErrors().size()
        );
    }

    @Test
    public void testHelpWithFeesValidatorNotValidForUploadAdditionalApplications() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId("123")
                             .state("PENDING").caseData(CaseData.builder()
                                                            .applicantSolicitorEmailAddress("hello@gmail.com")
                                                            .helpWithFees(YesOrNo.Yes)
                                                            .uploadAdditionalApplicationData(
                                                                UploadAdditionalApplicationData.builder()
                                                                    .additionalApplicationsHelpWithFees(YesOrNo.Yes)
                                                                    .additionalApplicationsHelpWithFeesNumber("$$$$$$")
                                                                    .build())
                                                            .build()).build())
            .eventId(Event.UPLOAD_ADDITIONAL_APPLICATIONS.getId())
            .build();
        when(feeAndPayServiceRequestService.validateSuppressedHelpWithFeesCheck(callbackRequest)).thenCallRealMethod();
        Assert.assertEquals(
            HWF_SUPPRESSION_ERROR_MESSAGE,
            feeAndPayServiceRequestController.helpWithFeesValidator(authToken, callbackRequest).getErrors().get(0)
        );
    }

    //TODO: Help with fees validation has been suppressed for the time being
    //    @Test
    //    public void testHelpWithFeesValidatorExpression1() {
    //        CallbackRequest callbackRequest = CallbackRequest.builder()
    //            .caseDetails(CaseDetails.builder().caseId("123")
    //                             .state("PENDING").caseData(CaseData.builder()
    //                                                            .applicantSolicitorEmailAddress("hello@gmail.com")
    //                                                            .helpWithFees(YesOrNo.Yes)
    //                                                            .helpWithFeesNumber("w12-f34-z98")
    //                                                            .build()).build()).build();
    //        Assert.assertNotNull(feeAndPayServiceRequestController.helpWithFeesValidator(authToken, callbackRequest));
    //    }
    //
    //    @Test
    //    public void testhelpWithFeesValidatorExpression2() {
    //        CallbackRequest callbackRequest = CallbackRequest.builder()
    //            .caseDetails(CaseDetails.builder().caseId("123")
    //                             .state("PENDING").caseData(CaseData.builder()
    //                                                            .applicantSolicitorEmailAddress("hello@gmail.com")
    //                                                            .helpWithFees(YesOrNo.Yes)
    //                                                            .helpWithFeesNumber("aW34-123456")
    //                                                            .build()).build()).build();
    //        Assert.assertNotNull(feeAndPayServiceRequestController.helpWithFeesValidator(authToken, callbackRequest));
    //    }
    //
    //    @Test
    //    public void testHelpWithFeesNoSelected() {
    //        CallbackRequest callbackRequest = CallbackRequest.builder()
    //            .caseDetails(CaseDetails.builder().caseId("123")
    //                             .state("PENDING").caseData(CaseData.builder()
    //                                                            .applicantSolicitorEmailAddress("hello@gmail.com")
    //                                                            .helpWithFees(YesOrNo.No)
    //                                                            .build()).build()).build();
    //        Assert.assertNotNull(feeAndPayServiceRequestController.helpWithFeesValidator(authToken, callbackRequest));
    //    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
