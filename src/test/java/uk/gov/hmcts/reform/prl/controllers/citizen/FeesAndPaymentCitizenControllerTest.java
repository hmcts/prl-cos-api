package uk.gov.hmcts.reform.prl.controllers.citizen;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.payment.CreatePaymentRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeResponseForCitizen;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentStatusResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

public class FeesAndPaymentCitizenControllerTest {

    @InjectMocks
    private FeesAndPaymentCitizenController feesAndPaymentCitizenController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private FeeService feeService;

    @Mock
    private FeeResponse feeResponse;

    @Mock
    private PaymentRequestService paymentRequestService;

    private FeeResponseForCitizen feeResponseForCitizen;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "TestS2sToken";
    public static final String PAYMENT_REFERENCE = "RC-1599-4778-4711-5958";
    public static final String REDIRECT_URL = "https://www.gov.uk";
    public static final String TEST_CASE_ID = "1656350492135029";
    public static final String APPLICANT_NAME = "APPLICANT_NAME";


    @Before
    public void setUp() {

        MockitoAnnotations.openMocks(this);

        feeResponse = FeeResponse.builder()
            .amount(BigDecimal.valueOf(232.00))
            .build();
    }

    @Test
    public void fetchFeeDetailsSuccessfully() throws Exception {
        feeResponseForCitizen = FeeResponseForCitizen.builder()
            .amount(feeResponse.getAmount().toString()).build();

        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        feesAndPaymentCitizenController.fetchFeesAmount(authToken, s2sToken);
        Assert.assertEquals(feeResponseForCitizen.getAmount(), feeResponse.getAmount().toString());
    }

    @Test
    public void fetchFeeDetailsWithInvalidClient() throws Exception {
        feeResponseForCitizen = FeeResponseForCitizen.builder()
            .amount(feeResponse.getAmount().toString()).build();
        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.FALSE);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        FeeResponseForCitizen feeResponseForCitizen = feesAndPaymentCitizenController.fetchFeesAmount(
            authToken,
            s2sToken
        );
        assertEquals("Invalid Client", feeResponseForCitizen.getErrorRetrievingResponse());
    }

    @Test
    public void createPaymentRequestSuccessfully() throws Exception {
        //Given
        CreatePaymentRequest createPaymentRequest = CreatePaymentRequest
                .builder().caseId(TEST_CASE_ID).returnUrl(REDIRECT_URL)
                .build();
        CallbackRequest callbackRequest = CallbackRequest
                .builder()
                .caseDetails(CaseDetails
                        .builder()
                        .caseId(TEST_CASE_ID)
                        .caseData(CaseData
                                .builder()
                                .id(Long.parseLong(TEST_CASE_ID))
                                .applicantCaseName(APPLICANT_NAME)
                                .build()).build())
                .build();
        PaymentServiceResponse paymentServiceResponse = PaymentServiceResponse.builder()
                .serviceRequestReference(PAYMENT_REFERENCE).build();
        PaymentResponse paymentResponse = PaymentResponse.builder()
                .paymentReference(PAYMENT_REFERENCE).build();

        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);
        when(paymentRequestService.createPayment(authToken,createPaymentRequest)).thenReturn(paymentResponse);

        //When
        PaymentResponse actualPaymentResponse = feesAndPaymentCitizenController
                .createPaymentRequest(authToken, s2sToken, createPaymentRequest);
        //Then
        assertEquals(paymentResponse, actualPaymentResponse);
    }


    @Test
    public void createPaymentRequestWithInvalidClient() throws Exception {
        //Given
        CreatePaymentRequest createPaymentRequest = CreatePaymentRequest
                .builder().caseId(TEST_CASE_ID).returnUrl(REDIRECT_URL)
                .build();

        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.FALSE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);

        //Then
        assertThrows(RuntimeException.class, () -> feesAndPaymentCitizenController
                .createPaymentRequest(authToken, s2sToken, createPaymentRequest));
    }


    @Test
    public void retrievePaymentStatusSuccessfully() throws Exception {

        PaymentStatusResponse paymentStatusResponse = PaymentStatusResponse.builder()
            .amount("232").reference(PAYMENT_REFERENCE)
            .ccdcaseNumber("1647959867368635").caseReference("1647959867368635")
            .channel("online").method("card").status("Success")
            .externalReference("uau4i1elcbmf36kshfp6f33npv")
            .paymentGroupReference("2022-1662471461349")
            .build();

        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);

        when(paymentRequestService.fetchPaymentStatus(authToken,PAYMENT_REFERENCE))
            .thenReturn(paymentStatusResponse);

        PaymentStatusResponse actualPaymentStatusResponse = feesAndPaymentCitizenController
            .retrievePaymentStatus(authToken,s2sToken,PAYMENT_REFERENCE,TEST_CASE_ID);

        assertEquals(paymentStatusResponse,actualPaymentStatusResponse);
    }

    @Test
    public void retrievePaymentStatusWithInvalidClient() throws Exception {

        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.FALSE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);

        //Then
        assertThrows(RuntimeException.class, () -> feesAndPaymentCitizenController
            .retrievePaymentStatus(authToken, s2sToken, PAYMENT_REFERENCE,TEST_CASE_ID));
    }

    @Test
    public void fetchFeeCodeSuccessfully() throws Exception {
        //Given
        feeResponseForCitizen = FeeResponseForCitizen.builder()
            .amount(feeResponse.getAmount().toString()).build();

        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);
        FeeRequest feeRequest = FeeRequest.builder().caseId("123").build();
        when(feeService.fetchFeeCode(feeRequest,authToken,s2sToken)).thenReturn(feeResponseForCitizen);

        //When
        FeeResponseForCitizen actualResponse = feesAndPaymentCitizenController
            .fetchFeeCode(authToken, s2sToken, feeRequest);
        //Then
        assertEquals(feeResponseForCitizen, actualResponse);
    }

    @Test
    public void fetchFeeCodeException() throws Exception {
        //Given
        feeResponseForCitizen = FeeResponseForCitizen.builder()
            .amount(feeResponse.getAmount().toString()).build();

        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);
        FeeRequest feeRequest = FeeRequest.builder().caseId("123").build();
        when(feeService.fetchFeeCode(feeRequest,authToken,s2sToken)).thenThrow(new RuntimeException());

        //When
        FeeResponseForCitizen actualResponse = feesAndPaymentCitizenController
            .fetchFeeCode(authToken, s2sToken, feeRequest);
        //Then
        assertEquals(feeResponseForCitizen.getErrorRetrievingResponse(), actualResponse.getErrorRetrievingResponse());

    }

    @Test
    public void fetchFeeCodeWithInvalidClient() throws Exception {
        //Given
        feeResponseForCitizen = FeeResponseForCitizen.builder().errorRetrievingResponse("Invalid Client").build();

        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.FALSE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);
        FeeRequest feeRequest = FeeRequest.builder().caseId("123").build();
        FeeResponseForCitizen actualResponse = feesAndPaymentCitizenController
            .fetchFeeCode(authToken, s2sToken, feeRequest);

        //Then
        assertEquals(feeResponseForCitizen.getErrorRetrievingResponse(), actualResponse.getErrorRetrievingResponse());

    }



}
