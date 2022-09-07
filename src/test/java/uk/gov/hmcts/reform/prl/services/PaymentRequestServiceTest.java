package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.PaymentApi;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.payment.CasePaymentRequestDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.OnlineCardPaymentRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.controllers.citizen.FeesAndPaymentCitizenControllerTest.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.prl.controllers.citizen.FeesAndPaymentCitizenControllerTest.REDIRECT_URL;
import static uk.gov.hmcts.reform.prl.services.PaymentRequestService.ENG_LANGUAGE;
import static uk.gov.hmcts.reform.prl.services.PaymentRequestService.GBP_CURRENCY;

@RunWith(SpringRunner.class)
public class PaymentRequestServiceTest {

    private final String serviceAuthToken = "Bearer testServiceAuth";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private PaymentApi paymentApi;

    @Mock
    private FeeService feeService;

    @InjectMocks
    PaymentRequestService paymentRequestService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    private FeeResponse feeResponse;

    @Mock
    private PaymentServiceResponse paymentServiceResponse;

    private CallbackRequest callbackRequest;

    private PaymentServiceRequest paymentServiceRequest;

    @Before
    public void setUp() throws Exception {
        feeResponse = FeeResponse.builder()
            .amount(BigDecimal.valueOf(100))
            .version(1)
            .code("FEE0325")
            .build();


        paymentServiceRequest = PaymentServiceRequest.builder()
            .callBackUrl(null)
            .casePaymentRequest(CasePaymentRequestDto.builder()
                                    .action(PrlAppsConstants.PAYMENT_ACTION)
                                    .responsibleParty("123").build())
            .caseReference(String.valueOf("123"))
            .ccdCaseNumber(String.valueOf("123"))
            .fees(new FeeDto[]{
                FeeDto.builder()
                    .calculatedAmount(feeResponse.getAmount())
                    .code(feeResponse.getCode())
                    .version(feeResponse.getVersion())
                    .volume(1).build()
            })
            .build();


        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .caseId("123")
                             .caseData(CaseData.builder()
                                           .applicantCaseName("123")
                                           .build())
                             .build())
            .build();


    }

    @Test
    public void shouldReturnPaymentServiceResponseWithReferenceResponse() throws Exception {
        when(objectMapper.convertValue(
            CaseData.builder().applicantCaseName(callbackRequest.getCaseDetails().getCaseData().getApplicantCaseName())
                .id(Long.valueOf(callbackRequest.getCaseDetails().getCaseId())).build(),
            CaseData.class
        )).thenReturn(CaseData.builder()
                          .applicantCaseName("123")
                          .id(Long.valueOf("123"))
                          .build());

        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);

        paymentServiceResponse = PaymentServiceResponse.builder().serviceRequestReference("response").build();

        when(paymentApi
                 .createPaymentServiceRequest("test token", "Bearer testServiceAuth", paymentServiceRequest))
            .thenReturn(paymentServiceResponse);

        PaymentServiceResponse psr = paymentRequestService.createServiceRequest(callbackRequest, "test token");
        assertNotNull(psr);
        assertEquals("response", psr.getServiceRequestReference());

    }

    @Test
    public void shouldReturnPaymentServiceResponseWithNullReference() throws Exception {
        when(objectMapper.convertValue(
            CaseData.builder().applicantCaseName(callbackRequest.getCaseDetails().getCaseData().getApplicantCaseName())
                .id(Long.valueOf(callbackRequest.getCaseDetails().getCaseId())).build(),
            CaseData.class
        )).thenReturn(CaseData.builder()
                          .applicantCaseName("123")
                          .id(Long.valueOf("123"))
                          .build());

        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);

        when(paymentApi.createPaymentServiceRequest("", "Bearer testServiceAuth", paymentServiceRequest)).thenReturn(
            paymentServiceResponse);

        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .caseId("123")
                             .caseData(CaseData.builder()
                                           .id(Long.valueOf("123456789"))
                                           .applicantCaseName("123")
                                           .build())
                             .build())
            .build();

        PaymentServiceResponse psr = paymentRequestService.createServiceRequest(callbackRequest, "");

        assertNull(psr.getServiceRequestReference());

    }

    @Test
    public void shouldThrowNullPointerException() throws Exception {
        callbackRequest = CallbackRequest.builder().build();
        assertThrows(NullPointerException.class, () -> {
            PaymentServiceResponse psr = paymentRequestService.createServiceRequest(callbackRequest, "");
        });
    }

    @Test
    public void shouldReturnPaymentCreateResponse() throws Exception {
        //Given
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        OnlineCardPaymentRequest onlineCardPaymentRequest = OnlineCardPaymentRequest
                .builder().returnUrl(REDIRECT_URL).amount(feeResponse.getAmount())
                .currency(GBP_CURRENCY).language(ENG_LANGUAGE).build();
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        PaymentResponse paymentResponse = PaymentResponse.builder()
                .paymentReference(PAYMENT_REFERENCE)
                .dateCreated("2020-09-07T11:24:07.160+0000")
                .externalReference("vnahehn9rlv17e5kel03pugd7j")
                .nextUrl("https://www.payments.service.gov.uk/secure/7a85745f-9485-47e4-ae12-e7d659a40299")
                .paymentStatus("Initiated")
                .build();
        when(paymentApi.createPaymentRequest(PAYMENT_REFERENCE, serviceAuthToken,
                serviceAuthToken, onlineCardPaymentRequest)).thenReturn(paymentResponse);

        //When
        PaymentResponse actualPaymentResponse = paymentRequestService
                .createServicePayment(PAYMENT_REFERENCE, serviceAuthToken, REDIRECT_URL);

        //Then
        assertEquals(paymentResponse, actualPaymentResponse);
    }
}

