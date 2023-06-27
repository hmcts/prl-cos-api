package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
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
import uk.gov.hmcts.reform.prl.services.FeeAndPayServiceRequestService;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;

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
    private FeeAndPayServiceRequestService feeAndPayServiceRequestService;

    public static final String authToken = "Bearer TestAuthToken";

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

        when(feesService.fetchFeeDetails(feeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

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
        ResponseEntity response = feeAndPayServiceRequestController.ccdSubmitted(authToken, callbackRequest);
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
        ResponseEntity response = feeAndPayServiceRequestController.ccdSubmitted(authToken, callbackRequest);
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
        when(feeAndPayServiceRequestService.validateHelpWithFeesNumber(callbackRequest)).thenCallRealMethod();
        Assert.assertEquals(
            "The help with fees number is incorrect",
            feeAndPayServiceRequestController.helpWithFeesValidator(authToken, callbackRequest).getErrors().get(0)
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
                                                                    .additionalApplicationsHelpWithFeesNumber("$$$$$$")
                                                                    .build())
                                                            .build()).build())
            .eventId(Event.UPLOAD_ADDITIONAL_APPLICATIONS.getId())
            .build();
        when(feeAndPayServiceRequestService.validateHelpWithFeesNumber(callbackRequest)).thenCallRealMethod();
        Assert.assertEquals(
            "The help with fees number is incorrect",
            feeAndPayServiceRequestController.helpWithFeesValidator(authToken, callbackRequest).getErrors().get(0)
        );
    }

    @Test
    public void testhelpWithFeesValidatorExpression1() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId("123")
                             .state("PENDING").caseData(CaseData.builder()
                                                            .applicantSolicitorEmailAddress("hello@gmail.com")
                                                            .helpWithFees(YesOrNo.Yes)
                                                            .helpWithFeesNumber("w12-f34-z98")
                                                            .build()).build()).build();
        Assert.assertNotNull(feeAndPayServiceRequestController.helpWithFeesValidator(authToken, callbackRequest));
    }

    @Test
    public void testhelpWithFeesValidatorExpression2() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId("123")
                             .state("PENDING").caseData(CaseData.builder()
                                                            .applicantSolicitorEmailAddress("hello@gmail.com")
                                                            .helpWithFees(YesOrNo.Yes)
                                                            .helpWithFeesNumber("aW34-123456")
                                                            .build()).build()).build();
        Assert.assertNotNull(feeAndPayServiceRequestController.helpWithFeesValidator(authToken, callbackRequest));
    }

    @Test
    public void testhelpWithFeesNoSlected() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId("123")
                             .state("PENDING").caseData(CaseData.builder()
                                                            .applicantSolicitorEmailAddress("hello@gmail.com")
                                                            .helpWithFees(YesOrNo.No)
                                                            .build()).build()).build();
        Assert.assertNotNull(feeAndPayServiceRequestController.helpWithFeesValidator(authToken, callbackRequest));
    }
}
