package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.PaymentApi;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.payment.CasePaymentRequestDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.CitizenAwpPayment;
import uk.gov.hmcts.reform.prl.models.dto.payment.CreatePaymentRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.OnlineCardPaymentRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentStatusResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestReferenceStatusResponse;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.controllers.citizen.FeesAndPaymentCitizenControllerTest.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.prl.controllers.citizen.FeesAndPaymentCitizenControllerTest.REDIRECT_URL;
import static uk.gov.hmcts.reform.prl.controllers.citizen.FeesAndPaymentCitizenControllerTest.authToken;
import static uk.gov.hmcts.reform.prl.services.PaymentRequestService.ENG_LANGUAGE;
import static uk.gov.hmcts.reform.prl.services.PaymentRequestService.GBP_CURRENCY;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PaymentRequestServiceTest {

    private final String serviceAuthToken = "Bearer testServiceAuth";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private PaymentApi paymentApi;

    @Mock
    private FeeService feeService;

    @InjectMocks
    private PaymentRequestService paymentRequestService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    private FeeResponse feeResponse;

    @Mock
    private PaymentServiceResponse paymentServiceResponse;

    @Mock
    private PaymentStatusResponse paymentStatusResponse;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AllTabServiceImpl allTabService;

    private CallbackRequest callbackRequest;
    private CreatePaymentRequest createPaymentRequest;
    private PaymentResponse paymentResponse;
    private PaymentServiceRequest paymentServiceRequest;
    public static final String TEST_CASE_ID = "1656350492135029";
    public static final String PAYMENTSRREFERENCENUMBER = "1647959867368635";
    public static final String PAYMENTREFERENCENUMBER = "RC-1662-4714-6207-7330";
    public static final String APPLICANT_NAME = "APPLICANT_NAME";
    private CaseData caseData;

    private OnlineCardPaymentRequest onlineCardPaymentRequest;


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
                                    .responsibleParty(APPLICANT_NAME).build())
            .caseReference(String.valueOf(TEST_CASE_ID))
            .ccdCaseNumber(String.valueOf(TEST_CASE_ID))
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
                             .caseId(TEST_CASE_ID)
                             .caseData(CaseData.builder()
                                           .id(Long.parseLong(TEST_CASE_ID))
                                           .applicantCaseName(APPLICANT_NAME)
                                           .build())
                             .build())
            .build();

        createPaymentRequest = CreatePaymentRequest.builder()
            .caseId(TEST_CASE_ID)
            .applicantCaseName(APPLICANT_NAME)
            .returnUrl(null)
            .feeType(FeeType.C100_SUBMISSION_FEE)
            .build();

        paymentResponse = PaymentResponse.builder()
            .paymentReference(PAYMENT_REFERENCE)
            .dateCreated("2020-09-07T11:24:07.160+0000")
            .externalReference("vnahehn9rlv17e5kel03pugd7j")
            .nextUrl("https://www.payments.service.gov.uk/secure/7a85745f-9485-47e4-ae12-e7d659a40299")
            .paymentStatus("Initiated")
            .build();

        onlineCardPaymentRequest = OnlineCardPaymentRequest
            .builder().returnUrl(null).amount(feeResponse.getAmount())
            .currency(GBP_CURRENCY).language(ENG_LANGUAGE).build();

        caseData = CaseData.builder()
            .id(Long.parseLong(TEST_CASE_ID))
            .applicantCaseName(APPLICANT_NAME)
                .paymentReferenceNumber(PAYMENTREFERENCENUMBER)
                .paymentServiceRequestReferenceNumber(PAYMENTSRREFERENCENUMBER)
            .build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseDetails, caseData, null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
    }

    @Test
    public void shouldReturnPaymentServiceResponseWithReferenceResponse() throws Exception {
        when(objectMapper.convertValue(
            CaseData.builder().applicantCaseName(callbackRequest.getCaseDetails().getCaseData().getApplicantCaseName())
                .id(Long.valueOf(callbackRequest.getCaseDetails().getCaseId())).build(),
            CaseData.class
        )).thenReturn(CaseData.builder()
                          .applicantCaseName(APPLICANT_NAME)
                          .id(Long.valueOf(TEST_CASE_ID))
                          .build());

        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);

        paymentServiceResponse = PaymentServiceResponse.builder().serviceRequestReference("response").build();

        when(paymentApi
                 .createPaymentServiceRequest("test token", "Bearer testServiceAuth", paymentServiceRequest))
            .thenReturn(paymentServiceResponse);

        PaymentServiceResponse psr = paymentRequestService.createServiceRequest(callbackRequest, "test token", feeResponse);
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
                          .applicantCaseName(APPLICANT_NAME)
                          .id(Long.valueOf(TEST_CASE_ID))
                          .build());

        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);

        when(paymentApi.createPaymentServiceRequest("", "Bearer testServiceAuth", paymentServiceRequest)).thenReturn(
            paymentServiceResponse);

        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .caseId(TEST_CASE_ID)
                             .caseData(CaseData.builder()
                                           .id(Long.valueOf(TEST_CASE_ID))
                                           .applicantCaseName(APPLICANT_NAME)
                                           .build())
                             .build())
            .build();

        PaymentServiceResponse psr = paymentRequestService.createServiceRequest(callbackRequest, "", feeResponse);

        assertNull(psr.getServiceRequestReference());

    }

    @Test
    public void shouldThrowNullPointerException() {
        callbackRequest = CallbackRequest.builder().build();
        assertThrows(NullPointerException.class, () ->
            paymentRequestService.createServiceRequest(callbackRequest, "", feeResponse));
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
                                             serviceAuthToken, onlineCardPaymentRequest
        )).thenReturn(paymentResponse);

        //When
        PaymentResponse actualPaymentResponse = paymentRequestService
            .createServicePayment(PAYMENT_REFERENCE, serviceAuthToken, REDIRECT_URL, feeResponse.getAmount());

        //Then
        assertEquals(paymentResponse, actualPaymentResponse);
    }

    @Test
    public void shouldReturnPaymentStatus() {

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);

        paymentStatusResponse = PaymentStatusResponse.builder()
            .amount("232").reference(PAYMENT_REFERENCE)
            .ccdcaseNumber("1647959867368635").caseReference("1647959867368635")
            .channel("online").method("card").status("Success")
            .externalReference("uau4i1elcbmf36kshfp6f33npv")
            .paymentGroupReference("2022-1662471461349")
            .build();
        when(paymentApi.fetchPaymentStatus(serviceAuthToken, serviceAuthToken, PAYMENT_REFERENCE))
            .thenReturn(paymentStatusResponse);

        //When
        PaymentStatusResponse actualPaymentStatusResponse = paymentRequestService
            .fetchPaymentStatus(serviceAuthToken, PAYMENT_REFERENCE);

        //Then
        assertEquals(paymentStatusResponse, actualPaymentStatusResponse);

    }

    @Test
    public void shouldCreateServiceRequestAndPaymentRequests() throws Exception {

        caseData = caseData.toBuilder()
            .paymentServiceRequestReferenceNumber(PAYMENT_REFERENCE)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(
            Long.parseLong(TEST_CASE_ID)).data(stringObjectMap).build();
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(
            CaseData.builder().applicantCaseName(APPLICANT_NAME)
                .id(Long.valueOf(TEST_CASE_ID)).build(),
            CaseData.class
        )).thenReturn(CaseData.builder().id(Long.parseLong(TEST_CASE_ID)).applicantCaseName(APPLICANT_NAME).build());
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, createPaymentRequest.getCaseId())).thenReturn(
            caseDetails);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        paymentServiceResponse = PaymentServiceResponse.builder().serviceRequestReference(PAYMENTSRREFERENCENUMBER).build();
        when(paymentApi.createPaymentServiceRequest(authToken, serviceAuthToken, paymentServiceRequest)).thenReturn(
            paymentServiceResponse);

        PaymentResponse paymentResponse = PaymentResponse.builder()
            .paymentReference(PAYMENT_REFERENCE)
            .dateCreated("2020-09-07T11:24:07.160+0000")
            .externalReference("vnahehn9rlv17e5kel03pugd7j")
            .nextUrl("https://www.payments.service.gov.uk/secure/7a85745f-9485-47e4-ae12-e7d659a40299")
            .paymentStatus("Initiated")
            .build();
        when(paymentApi.createPaymentRequest(PAYMENT_REFERENCE, serviceAuthToken,
                                             serviceAuthToken, onlineCardPaymentRequest
        )).thenReturn(paymentResponse);

        caseData = caseData.toBuilder()
            .paymentServiceRequestReferenceNumber(paymentServiceResponse.getServiceRequestReference())
            .paymentReferenceNumber(paymentResponse.getPaymentReference())
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);

        assertNotNull(paymentResponse);
        assertNotNull(paymentResponse.getPaymentReference());

    }

    @Test
    public void shouldCreatePaymentRequestIfServiceRequestExists() throws Exception {

        caseData = caseData.toBuilder().build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(
            CaseData.builder().applicantCaseName(APPLICANT_NAME)
                .id(Long.valueOf(TEST_CASE_ID)).build(),
            CaseData.class
        )).thenReturn(CaseData.builder().id(Long.parseLong(TEST_CASE_ID)).applicantCaseName(APPLICANT_NAME).build());
        paymentServiceResponse = PaymentServiceResponse.builder().serviceRequestReference(PAYMENTREFERENCENUMBER).build();
        when(paymentApi.fetchPaymentStatus(authToken, serviceAuthToken, PAYMENTREFERENCENUMBER)).thenReturn(
            PaymentStatusResponse.builder().status("Success").build());

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(
            Long.parseLong(TEST_CASE_ID)).data(stringObjectMap).build();
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, createPaymentRequest.getCaseId())).thenReturn(
            caseDetails);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        paymentServiceResponse = PaymentServiceResponse.builder().serviceRequestReference(PAYMENTSRREFERENCENUMBER).build();
        when(paymentApi.createPaymentServiceRequest(authToken, serviceAuthToken, paymentServiceRequest)).thenReturn(
            paymentServiceResponse);
        when(paymentApi.createPaymentRequest(
            paymentServiceResponse.getServiceRequestReference(),
            authToken,
            serviceAuthToken,
            onlineCardPaymentRequest
        )).thenReturn(paymentResponse);
        caseData = caseData.builder()
            .paymentServiceRequestReferenceNumber(paymentServiceResponse.getServiceRequestReference())
                .paymentReferenceNumber(paymentResponse.getPaymentReference())
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);

        PaymentResponse paymentResponse = paymentRequestService.createPayment(
            authToken,
            createPaymentRequest
        );
        assertNotNull(paymentResponse);
        assertNotNull(paymentResponse.getPaymentReference());

    }

    @Test
    public void testCreateFeesWithHelpWithFeesNewRefGenerated() throws Exception {
        createPaymentRequest = CreatePaymentRequest.builder()
            .caseId("12345")
            .returnUrl(null)
            .feeType(FeeType.C100_SUBMISSION_FEE).build();
        CaseData newCaseData = CaseData.builder().paymentServiceRequestReferenceNumber("12345").build();
        Map<String, Object> stringObjectMap = newCaseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails =
            uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                .id(Long.parseLong(TEST_CASE_ID)).data(stringObjectMap)
                .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(newCaseData);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, createPaymentRequest
            .getCaseId())).thenReturn(caseDetails);

        when(feeService.fetchFeeDetails(any(FeeType.class))).thenReturn(feeResponse);

        onlineCardPaymentRequest = OnlineCardPaymentRequest
            .builder().returnUrl(null).amount(feeResponse.getAmount())
            .currency(GBP_CURRENCY).language(ENG_LANGUAGE).build();

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(paymentApi.createPaymentRequest(anyString(),
                                             anyString(),
                                             anyString(),
                                             any(OnlineCardPaymentRequest.class)))
            .thenReturn(PaymentResponse.builder().build());

        assertNotNull(paymentRequestService.createPayment(authToken, createPaymentRequest));

    }

    @Test
    public void testCreateFeesWithHelpWithFees() throws Exception {
        createPaymentRequest = CreatePaymentRequest.builder()
            .hwfRefNumber("test")
            .caseId("12345")
            .feeType(FeeType.C100_SUBMISSION_FEE)
            .build();
        CaseData newCaseData = CaseData.builder().paymentServiceRequestReferenceNumber("12345").build();
        Map<String, Object> stringObjectMap = newCaseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails =
            uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                .id(Long.parseLong(TEST_CASE_ID)).data(stringObjectMap)
                .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(newCaseData);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, createPaymentRequest.getCaseId())).thenReturn(caseDetails);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        PaymentResponse paymentResponseReturned = paymentRequestService.createPayment(authToken, createPaymentRequest);
        assertNotNull(paymentResponseReturned.getServiceRequestReference());
    }

    @Test
    public void shouldCreatePaymentRequestWithPaymentNull() throws Exception {
        paymentServiceRequest = PaymentServiceRequest.builder()
            .callBackUrl(null)
            .casePaymentRequest(CasePaymentRequestDto.builder()
                                    .action(PrlAppsConstants.PAYMENT_ACTION)
                                    .responsibleParty(APPLICANT_NAME).build())
            .caseReference(String.valueOf(TEST_CASE_ID))
            .ccdCaseNumber(String.valueOf(TEST_CASE_ID))
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
                             .caseId(TEST_CASE_ID)
                             .caseData(CaseData.builder()
                                           .id(Long.parseLong(TEST_CASE_ID))
                                           .applicantCaseName(APPLICANT_NAME)
                                           .build())
                             .build())
            .build();

        createPaymentRequest = CreatePaymentRequest.builder().caseId(TEST_CASE_ID)
            .applicantCaseName(APPLICANT_NAME).returnUrl(null).feeType(FeeType.C100_SUBMISSION_FEE).build();

        paymentResponse = PaymentResponse.builder()
            .paymentReference(PAYMENT_REFERENCE)
            .dateCreated("2020-09-07T11:24:07.160+0000")
            .externalReference("vnahehn9rlv17e5kel03pugd7j")
            .nextUrl("https://www.payments.service.gov.uk/secure/7a85745f-9485-47e4-ae12-e7d659a40299")
            .paymentStatus("Initiated")
            .build();

        onlineCardPaymentRequest = OnlineCardPaymentRequest
            .builder().returnUrl(null).amount(feeResponse.getAmount())
            .currency(GBP_CURRENCY).language(ENG_LANGUAGE).build();

        caseData = CaseData.builder()
            .id(Long.parseLong(TEST_CASE_ID))
            .applicantCaseName(APPLICANT_NAME)
            .paymentReferenceNumber(null)
            .paymentServiceRequestReferenceNumber(null)
            .build();
        caseData = caseData.toBuilder().build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(
            Long.parseLong(TEST_CASE_ID)).data(stringObjectMap).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(
            CaseData.builder().applicantCaseName(APPLICANT_NAME)
                .id(Long.valueOf(TEST_CASE_ID)).build(),
            CaseData.class
        )).thenReturn(CaseData.builder().id(Long.parseLong(TEST_CASE_ID)).applicantCaseName(APPLICANT_NAME).build());

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, createPaymentRequest.getCaseId())).thenReturn(
            caseDetails);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        paymentServiceResponse = PaymentServiceResponse.builder().serviceRequestReference(PAYMENTSRREFERENCENUMBER).build();
        when(paymentApi.createPaymentServiceRequest(authToken, serviceAuthToken, paymentServiceRequest)).thenReturn(
            paymentServiceResponse);
        when(paymentApi.createPaymentRequest(
            paymentServiceResponse.getServiceRequestReference(),
            authToken,
            serviceAuthToken,
            onlineCardPaymentRequest
        )).thenReturn(paymentResponse);
        caseData = caseData.toBuilder()
            .paymentServiceRequestReferenceNumber(null)
            .paymentReferenceNumber(null)
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);

        PaymentResponse paymentResponse = paymentRequestService.createPayment(
            authToken,
            createPaymentRequest
        );
        assertNotNull(paymentResponse);
        createPaymentRequest.setHwfRefNumber("referNumber");
        paymentResponse = paymentRequestService.createPayment(
            authToken,
            createPaymentRequest
        );
        assertNotNull(paymentResponse);
    }

    @Test
    public void shouldCreatePaymentRequestWithHwfPaymentReference() throws Exception {
        paymentServiceRequest = PaymentServiceRequest.builder()
            .callBackUrl(null)
            .casePaymentRequest(CasePaymentRequestDto.builder()
                                    .action(PrlAppsConstants.PAYMENT_ACTION)
                                    .responsibleParty(APPLICANT_NAME).build())
            .caseReference(String.valueOf(TEST_CASE_ID))
            .ccdCaseNumber(String.valueOf(TEST_CASE_ID))
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
                             .caseId(TEST_CASE_ID)
                             .caseData(CaseData.builder()
                                           .id(Long.parseLong(TEST_CASE_ID))
                                           .applicantCaseName(APPLICANT_NAME)
                                           .build())
                             .build())
            .build();

        createPaymentRequest = CreatePaymentRequest.builder().caseId(TEST_CASE_ID)
            .returnUrl(null)
            .hwfRefNumber("HWF123")
            .feeType(FeeType.C100_SUBMISSION_FEE)
            .build();

        paymentResponse = PaymentResponse.builder()
            .paymentReference(PAYMENT_REFERENCE)
            .dateCreated("2020-09-07T11:24:07.160+0000")
            .externalReference("vnahehn9rlv17e5kel03pugd7j")
            .nextUrl("https://www.payments.service.gov.uk/secure/7a85745f-9485-47e4-ae12-e7d659a40299")
            .paymentStatus("Initiated")
            .build();

        onlineCardPaymentRequest = OnlineCardPaymentRequest
            .builder().returnUrl(null).amount(feeResponse.getAmount())
            .currency(GBP_CURRENCY).language(ENG_LANGUAGE).build();

        caseData = CaseData.builder()
            .id(Long.parseLong(TEST_CASE_ID))
            .applicantCaseName(APPLICANT_NAME)
            .paymentReferenceNumber(null)
            .paymentServiceRequestReferenceNumber("test payment ref")
            .build();
        caseData = caseData.toBuilder().build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(
            Long.parseLong(TEST_CASE_ID)).data(stringObjectMap).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(
            CaseData.builder().applicantCaseName(APPLICANT_NAME)
                .id(Long.valueOf(TEST_CASE_ID)).build(),
            CaseData.class
        )).thenReturn(CaseData.builder().id(Long.parseLong(TEST_CASE_ID)).applicantCaseName(APPLICANT_NAME).build());

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, createPaymentRequest.getCaseId())).thenReturn(
            caseDetails);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        paymentServiceResponse = PaymentServiceResponse.builder().serviceRequestReference(PAYMENTSRREFERENCENUMBER).build();
        when(paymentApi.createPaymentServiceRequest(authToken, serviceAuthToken, paymentServiceRequest)).thenReturn(
            paymentServiceResponse);
        when(paymentApi.createPaymentRequest(
            paymentServiceResponse.getServiceRequestReference(),
            authToken,
            serviceAuthToken,
            onlineCardPaymentRequest
        )).thenReturn(paymentResponse);
        caseData = caseData.builder()
            .paymentServiceRequestReferenceNumber(null)
            .paymentReferenceNumber(null)
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);

        PaymentResponse paymentResponse = paymentRequestService.createPayment(
            authToken,
            createPaymentRequest
        );
        assertNotNull(paymentResponse);
        createPaymentRequest.setHwfRefNumber("referNumber");
        paymentResponse = paymentRequestService.createPayment(
            authToken,
            createPaymentRequest
        );
        assertNotNull(paymentResponse);
    }

    @Test
    public void shouldCreatePaymentRequestWithPaymentReferenceNumberNull() throws Exception {
        paymentServiceRequest = PaymentServiceRequest.builder()
            .callBackUrl(null)
            .casePaymentRequest(CasePaymentRequestDto.builder()
                                    .action(PrlAppsConstants.PAYMENT_ACTION)
                                    .responsibleParty(APPLICANT_NAME).build())
            .caseReference(String.valueOf(TEST_CASE_ID))
            .ccdCaseNumber(String.valueOf(TEST_CASE_ID))
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
                             .caseId(TEST_CASE_ID)
                             .caseData(CaseData.builder()
                                           .id(Long.parseLong(TEST_CASE_ID))
                                           .applicantCaseName(APPLICANT_NAME)
                                           .build())
                             .build())
            .build();

        createPaymentRequest = CreatePaymentRequest.builder().caseId(TEST_CASE_ID).applicantCaseName(APPLICANT_NAME)
            .returnUrl(null).feeType(FeeType.C100_SUBMISSION_FEE).build();

        paymentResponse = PaymentResponse.builder()
            .paymentReference(PAYMENT_REFERENCE)
            .dateCreated("2020-09-07T11:24:07.160+0000")
            .externalReference("vnahehn9rlv17e5kel03pugd7j")
            .nextUrl("https://www.payments.service.gov.uk/secure/7a85745f-9485-47e4-ae12-e7d659a40299")
            .paymentStatus("Initiated")
            .build();

        onlineCardPaymentRequest = OnlineCardPaymentRequest
            .builder().returnUrl(null).amount(feeResponse.getAmount())
            .currency(GBP_CURRENCY).language(ENG_LANGUAGE).build();

        caseData = CaseData.builder()
            .id(Long.parseLong(TEST_CASE_ID))
            .applicantCaseName(APPLICANT_NAME)
            .paymentReferenceNumber(PAYMENTREFERENCENUMBER)
            .paymentServiceRequestReferenceNumber(PAYMENTSRREFERENCENUMBER)
            .build();
        caseData = caseData.toBuilder().build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(
            Long.parseLong(TEST_CASE_ID)).data(stringObjectMap).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(
            CaseData.builder().applicantCaseName(APPLICANT_NAME)
                .id(Long.valueOf(TEST_CASE_ID)).build(),
            CaseData.class
        )).thenReturn(CaseData.builder().id(Long.parseLong(TEST_CASE_ID)).applicantCaseName(APPLICANT_NAME).build());

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, createPaymentRequest.getCaseId())).thenReturn(
            caseDetails);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        paymentServiceResponse = PaymentServiceResponse.builder().serviceRequestReference(PAYMENTSRREFERENCENUMBER).build();
        when(paymentApi.createPaymentServiceRequest(authToken, serviceAuthToken, paymentServiceRequest)).thenReturn(
            paymentServiceResponse);
        when(paymentApi.createPaymentRequest(
            paymentServiceResponse.getServiceRequestReference(),
            authToken,
            serviceAuthToken,
            onlineCardPaymentRequest
        )).thenReturn(paymentResponse);
        caseData = caseData.builder()
            .paymentServiceRequestReferenceNumber(null)
            .paymentReferenceNumber(PAYMENTREFERENCENUMBER)
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);

        PaymentResponse paymentResponse = paymentRequestService.createPayment(
            authToken,
            createPaymentRequest
        );

        assertNotNull(paymentResponse);
        createPaymentRequest.setHwfRefNumber("refer");
        paymentResponse = paymentRequestService.createPayment(
            authToken,
            createPaymentRequest
        );
        assertNotNull(paymentResponse);
    }


    @Test
    public void shouldPaymentRequestAgainIfPreviousPaymentFailed() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(
            Long.parseLong(TEST_CASE_ID)).data(stringObjectMap).build();
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        doReturn(caseData).when(objectMapper).convertValue(
            CaseData.builder().applicantCaseName(APPLICANT_NAME)
                .id(Long.valueOf(TEST_CASE_ID)).build(),
            CaseData.class
        );
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, createPaymentRequest.getCaseId())).thenReturn(
            caseDetails);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        paymentServiceResponse = PaymentServiceResponse.builder().serviceRequestReference(PAYMENTSRREFERENCENUMBER).build();
        when(paymentApi.createPaymentServiceRequest(authToken, serviceAuthToken, paymentServiceRequest)).thenReturn(
            paymentServiceResponse);
        doReturn(paymentResponse).when(paymentApi).createPaymentRequest(
            paymentServiceResponse.getServiceRequestReference(),
            authToken,
            serviceAuthToken,
            onlineCardPaymentRequest
        );
        caseData = caseData.builder()
            .paymentServiceRequestReferenceNumber(paymentServiceResponse.getServiceRequestReference())
            .paymentReferenceNumber(PAYMENTREFERENCENUMBER)
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);

        paymentStatusResponse = PaymentStatusResponse.builder()
            .amount("232").reference(PAYMENTREFERENCENUMBER)
            .ccdcaseNumber(TEST_CASE_ID).caseReference(TEST_CASE_ID)
            .channel("online").method("card").status("Failed")
            .externalReference("uau4i1elcbmf36kshfp6f33npv")
            .paymentGroupReference("2022-1662471461349")
            .build();
        doReturn(paymentStatusResponse).when(paymentApi).fetchPaymentStatus(
            authToken,
            serviceAuthToken,
            PAYMENTREFERENCENUMBER
        );

        PaymentResponse paymentResponse = paymentRequestService.createPayment(
            authToken,
            createPaymentRequest
        );
        assertNotNull(paymentResponse);
        assertNotNull(paymentResponse.getPaymentReference());
    }

    @Test
    public void shouldTestCreatePaymentRequestWhenHelpWithFeesApplied() throws Exception {

        caseData = caseData.toBuilder()
                .paymentServiceRequestReferenceNumber(null)
                .paymentReferenceNumber(null)
                .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(
                Long.parseLong(TEST_CASE_ID)).data(stringObjectMap).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(
                CaseData.builder().applicantCaseName(APPLICANT_NAME)
                        .id(Long.parseLong(TEST_CASE_ID)).build(),
                CaseData.class
        )).thenReturn(CaseData.builder().id(Long.parseLong(TEST_CASE_ID)).applicantCaseName(APPLICANT_NAME).build());

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);

        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, createPaymentRequest.getCaseId())).thenReturn(
                caseDetails);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        paymentServiceResponse = PaymentServiceResponse.builder().serviceRequestReference(PAYMENTSRREFERENCENUMBER).build();
        when(paymentApi.createPaymentServiceRequest(authToken, serviceAuthToken, paymentServiceRequest)).thenReturn(
                paymentServiceResponse);

        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);

        paymentResponse = paymentRequestService.createPayment(authToken,
                                                              createPaymentRequest.toBuilder().hwfRefNumber("TEST_HWF_REF").build());

        assertNotNull(paymentResponse);
        assertEquals(PAYMENTSRREFERENCENUMBER, paymentResponse.getServiceRequestReference());
    }

    @Test
    public void shouldTestCreatePaymentRequestWhenServiceAndPaymentReferenceAlreadyExist() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(
                Long.parseLong(TEST_CASE_ID)).data(stringObjectMap).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(
                CaseData.builder().applicantCaseName(APPLICANT_NAME)
                        .id(Long.parseLong(TEST_CASE_ID)).build(),
                CaseData.class
        )).thenReturn(CaseData.builder().id(Long.parseLong(TEST_CASE_ID)).applicantCaseName(APPLICANT_NAME).build());

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, createPaymentRequest.getCaseId())).thenReturn(
                caseDetails);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        paymentServiceResponse = PaymentServiceResponse.builder().serviceRequestReference(PAYMENTREFERENCENUMBER).build();
        when(paymentApi.fetchPaymentStatus(authToken, serviceAuthToken, PAYMENTREFERENCENUMBER)).thenReturn(
                PaymentStatusResponse.builder().status("Success").build());

        caseData = caseData.toBuilder()
                .paymentReferenceNumber(paymentResponse.getPaymentReference())
                .build();

        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);

        paymentResponse = paymentRequestService.createPayment(
            authToken,
            createPaymentRequest);

        assertNotNull(paymentResponse);
        assertEquals(PAYMENTSRREFERENCENUMBER, paymentResponse.getServiceRequestReference());
        assertEquals(PAYMENTREFERENCENUMBER, paymentResponse.getPaymentReference());
        assertEquals("Success", paymentResponse.getPaymentStatus());
    }

    @Test
    public void testCreateServiceRequestFromCcdCallack() throws Exception {
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(paymentApi.createPaymentServiceRequest(anyString(), anyString(), any(PaymentServiceRequest.class)))
            .thenReturn(PaymentServiceResponse.builder().serviceRequestReference("response").build());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest ccdCallbackRequest
            = uk.gov.hmcts.reform.ccd.client.model.CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().data(stringObjectMap)
                             .build()).build();

        PaymentServiceResponse paymentServiceResponse = paymentRequestService
            .createServiceRequestFromCcdCallack(ccdCallbackRequest, authToken);
        assertEquals("response", paymentServiceResponse.getServiceRequestReference());
    }

    @Test
    public void shouldReturnPaymentGroupReferenceStatus() {

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);

        ServiceRequestReferenceStatusResponse serviceRequestReferenceStatusResponse = ServiceRequestReferenceStatusResponse.builder()
            .serviceRequestReference("2024-1750000072989")
            .serviceRequestStatus("Paid")
            .build();
        when(paymentApi.fetchPaymentGroupReferenceStatus(serviceAuthToken, serviceAuthToken, "2024-1750000072989"))
            .thenReturn(serviceRequestReferenceStatusResponse);

        //When
        ServiceRequestReferenceStatusResponse actualServiceRequestReferenceStatusResponse = paymentRequestService
            .fetchServiceRequestReferenceStatus(serviceAuthToken, "2024-1750000072989");

        //Then
        assertEquals(serviceRequestReferenceStatusResponse, actualServiceRequestReferenceStatusResponse);

    }

    @Test
    public void testCreatePaymentWithNullHwfReferenceNumber() {
        when(paymentApi.createPaymentServiceRequest(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(PaymentServiceResponse.builder().serviceRequestReference("response").build());
        when(paymentApi.createPaymentRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(PaymentResponse.builder().build());
        PaymentResponse paymentResponse1 = paymentRequestService.createPayment(authToken, createPaymentRequest, null,
                                                                              null, feeResponse);
        assertEquals("response", paymentResponse1.getServiceRequestReference());
    }

    @Test
    public void testCreatePaymentWithNonNullHwfReferenceNumber() {
        when(paymentApi.createPaymentServiceRequest(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(PaymentServiceResponse.builder().serviceRequestReference("response").build());
        createPaymentRequest.setHwfRefNumber("test");
        PaymentResponse paymentResponse1 = paymentRequestService.createPayment(authToken, createPaymentRequest, null,
                                                                               null, feeResponse);
        assertEquals("response", paymentResponse1.getServiceRequestReference());
    }

    @Test
    public void testCreatePaymentWithNonNullPaymentServiceAndNullHwfReferenceNumber() {
        when(paymentApi.createPaymentRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(PaymentResponse.builder().build());
        PaymentResponse paymentResponse1 = paymentRequestService.createPayment(authToken, createPaymentRequest, "test",
                                                                               null, feeResponse);
        assertEquals("test", paymentResponse1.getServiceRequestReference());
    }

    @Test
    public void testCreatePaymentWithNonNullPaymentServiceAndNonNullHwfReferenceNumber() {
        when(paymentApi.createPaymentServiceRequest(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(PaymentServiceResponse.builder().serviceRequestReference("response").build());
        createPaymentRequest.setHwfRefNumber("test");
        PaymentResponse paymentResponse1 = paymentRequestService.createPayment(authToken, createPaymentRequest, "test",
                                                                               null, feeResponse);
        assertEquals("test", paymentResponse1.getServiceRequestReference());
    }

    @Test
    public void testCreatePaymentWithNullCaseData() throws Exception {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        null, null, null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        PaymentResponse paymentResponse1 = paymentRequestService.createPayment(authToken, createPaymentRequest);
        assertNull(paymentResponse1);
    }

    @Test
    public void testCreatePaymentWithNullFeeResponse() throws Exception {
        when(feeService.fetchFeeDetails(Mockito.any())).thenReturn(null);

        PaymentResponse paymentResponse1 = paymentRequestService.createPayment(authToken, createPaymentRequest);
        assertNull(paymentResponse1);
    }

    @Test
    public void testCreatePaymentWithNonAwpFeeResponse() throws Exception {
        when(feeService.fetchFeeDetails(Mockito.any())).thenReturn(feeResponse);
        when(paymentApi.createPaymentRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(PaymentResponse.builder().build());
        when(paymentApi.createPaymentServiceRequest(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(PaymentServiceResponse.builder().serviceRequestReference("response").build());
        createPaymentRequest = createPaymentRequest.toBuilder().feeType(FeeType.C2_WITH_NOTICE).build();
        PaymentResponse paymentResponse1 = paymentRequestService.createPayment(authToken, createPaymentRequest);
        assertEquals("response", paymentResponse1.getServiceRequestReference());
    }

    @Test
    public void testCreatePaymentWithAwpPayments() throws Exception {
        List<Element<CitizenAwpPayment>> awpPayments = new ArrayList<>();
        awpPayments.add(Element.<CitizenAwpPayment>builder().value(CitizenAwpPayment.builder()
                                                                       .feeType(FeeType.C2_WITH_NOTICE.toString())
                                                                       .awpType("test")
                                                                       .partType("test")
                                                                       .build()).build());
        CaseData caseDataUpdated = caseData.toBuilder()
            .citizenAwpPayments(awpPayments)
            .build();
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        new HashMap<>(),
                                                                                                        caseDataUpdated,
                                                                                                        null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        when(feeService.fetchFeeDetails(Mockito.any())).thenReturn(feeResponse);
        when(paymentApi.createPaymentRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(PaymentResponse.builder().build());
        when(paymentApi.createPaymentServiceRequest(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(PaymentServiceResponse.builder().serviceRequestReference("response").build());
        createPaymentRequest = createPaymentRequest.toBuilder()
            .partyType("test")
            .awpType("test")
            .feeType(FeeType.C2_WITH_NOTICE).build();
        PaymentResponse paymentResponse1 = paymentRequestService.createPayment(authToken, createPaymentRequest);
        assertEquals("response", paymentResponse1.getServiceRequestReference());
    }
}

