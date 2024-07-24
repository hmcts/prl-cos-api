package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.clients.PaymentApi;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.payment.CasePaymentRequestDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.CreatePaymentRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.OnlineCardPaymentRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentStatusResponse;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.math.BigDecimal;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PAYMENT_ACTION;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentRequestService {

    private final PaymentApi paymentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final FeeService feeService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final ObjectMapper objectMapper;
    public static final String GBP_CURRENCY = "GBP";
    public static final String ENG_LANGUAGE = "English";
    private static final String SERVICE_AUTH = "ServiceAuthorization";
    private static final String PAYMENT_STATUS_SUCCESS = "Success";
    private PaymentResponse paymentResponse;
    private final AllTabServiceImpl allTabService;

    @Value("${payments.api.callback-url}")
    String callBackUrl;

    public PaymentServiceResponse createServiceRequest(CallbackRequest callbackRequest,
                                                       String authorisation,
                                                       FeeResponse feeResponse) {
        CaseData caseData = objectMapper.convertValue(
            CaseData.builder().applicantCaseName(callbackRequest.getCaseDetails().getCaseData().getApplicantCaseName())
                .id(Long.parseLong(callbackRequest.getCaseDetails().getCaseId())).build(),
            CaseData.class
        );

        return getPaymentServiceResponse(authorisation, caseData, feeResponse);
    }

    public PaymentResponse createServicePayment(String serviceRequestReference,
                                                String authorization,
                                                String returnUrl,
                                                BigDecimal feeAmount) {
        return paymentApi
            .createPaymentRequest(serviceRequestReference, authorization, authTokenGenerator.generate(),
                                  OnlineCardPaymentRequest.builder()
                                      .amount(feeAmount)
                                      .currency(GBP_CURRENCY)
                                      .language(ENG_LANGUAGE)
                                      .returnUrl(returnUrl)
                                      .build()
            );
    }

    public PaymentStatusResponse fetchPaymentStatus(String authorization,
                                                    String paymentReference) {
        return paymentApi
            .fetchPaymentStatus(authorization, authTokenGenerator.generate(),
                                paymentReference
            );
    }

    public PaymentResponse createPayment(String authorization,
                                         CreatePaymentRequest createPaymentRequest) throws Exception {
        log.info("Inside createPayment -> request {}", createPaymentRequest);
        log.info("Retrieving caseData for caseId : {}", createPaymentRequest.getCaseId());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificEvent(
                createPaymentRequest.getCaseId(),
                CITIZEN_CASE_UPDATE.getValue()
            );
        CaseData caseData = startAllTabsUpdateDataContent.caseData();
        Map<String, Object> caseDataMap = startAllTabsUpdateDataContent.caseDataMap();

        if (null == caseData) {
            log.info(
                "Retrieved caseData is null for caseId {}, please provide a valid caseId",
                createPaymentRequest.getCaseId()
            );
            return null;
        }
        FeeResponse feeResponse = feeService.fetchFeeDetails(createPaymentRequest.getFeeType());
        if (null == feeResponse) {
            log.info("Error in fetching fee details for feeType {}", createPaymentRequest.getFeeType());
            return null;
        }
        createPaymentRequest = createPaymentRequest.toBuilder()
            .applicantCaseName(caseData.getApplicantCaseName()).build();

        if (isApplicationNotAwp(createPaymentRequest)) {
            log.info("*** Citizen C100 and other applications case payment ***");
            paymentResponse = handleC100Payment(
                authorization,
                caseData,
                caseDataMap,
                createPaymentRequest,
                feeResponse
            );
        } else {
            log.info("*** Citizen awp payment *** ** call awp method here");
        }
        //update case
        allTabService.submitAllTabsUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            createPaymentRequest.getCaseId(),
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataMap
        );

        return paymentResponse;
    }

    private boolean isApplicationNotAwp(CreatePaymentRequest createPaymentRequest) {
        return createPaymentRequest.getFeeType() == FeeType.C100_SUBMISSION_FEE
            || createPaymentRequest.getFeeType() == FeeType.PARENTAL_ORDER
            || createPaymentRequest.getFeeType() == FeeType.DECLARATION_OF_PARENTAGE
            || createPaymentRequest.getFeeType() == FeeType.SPECIAL_GUARDIANSHIP_ORDER;
    }

    private PaymentResponse handleC100Payment(String authorization,
                                              CaseData caseData,
                                              Map<String, Object> caseDataMap,
                                              CreatePaymentRequest createPaymentRequest,
                                              FeeResponse feeResponse) {
        paymentResponse = createPayment(
            authorization,
            createPaymentRequest,
            caseData.getPaymentServiceRequestReferenceNumber(),
            caseData.getPaymentReferenceNumber(),
            feeResponse
        );

        //update service request & payment request reference
        caseDataMap.put("paymentServiceRequestReferenceNumber", paymentResponse.getServiceRequestReference());
        caseDataMap.put("paymentReferenceNumber", paymentResponse.getPaymentReference());

        return paymentResponse;
    }

    public PaymentResponse createPayment(String authorization,
                                         CreatePaymentRequest createPaymentRequest,
                                         String paymentServiceReferenceNumber,
                                         String paymentReferenceNumber,
                                         FeeResponse feeResponse) {
        String caseId = createPaymentRequest.getCaseId();

        if (null == paymentServiceReferenceNumber
            && null == paymentReferenceNumber) {
            //Create dummy caseData with id & name
            CaseData caseData = CaseData.builder()
                .id(Long.parseLong(createPaymentRequest.getCaseId()))
                .applicantCaseName(createPaymentRequest.getApplicantCaseName())
                .build();

            if (null != createPaymentRequest.getHwfRefNumber()) {
                log.info("Help with fees is opted, first time submission -> creating only service request for the case id: {}", caseId);
                //create service request
                PaymentServiceResponse paymentServiceResponse = getPaymentServiceResponse(authorization, caseData, feeResponse);
                paymentResponse = PaymentResponse.builder()
                    .serviceRequestReference(paymentServiceResponse.getServiceRequestReference())
                    .build();
            } else {
                // if CR and PR doesn't exist
                log.info("Creating new service request and payment request for card payment 1st time for the case id: {}", caseId);
                //create service request
                PaymentServiceResponse paymentServiceResponse = getPaymentServiceResponse(authorization, caseData, feeResponse);
                paymentResponse = createServicePayment(paymentServiceResponse.getServiceRequestReference(),
                                                       authorization, createPaymentRequest.getReturnUrl(), feeResponse.getAmount());
                //set service request ref
                paymentResponse.setServiceRequestReference(paymentServiceResponse.getServiceRequestReference());
            }
            return paymentResponse;
        } else if (null != paymentServiceReferenceNumber
            && null == paymentReferenceNumber) {
            if (null != createPaymentRequest.getHwfRefNumber()) {
                log.info("Help with fees is opted, resubmit/retry scenario for the case id: {}", caseId);
                paymentResponse = PaymentResponse.builder()
                    .serviceRequestReference(paymentServiceReferenceNumber)
                    .build();
            } else {
                log.info("Creating new payment ref, resubmission for card payments for the case id: {} ", caseId);
                paymentResponse = createServicePayment(paymentServiceReferenceNumber,
                                                       authorization,
                                                       createPaymentRequest.getReturnUrl(),
                                                       feeResponse.getAmount());
                paymentResponse.setServiceRequestReference(paymentServiceReferenceNumber);
            }
            return paymentResponse;
        } else {
            return getPaymentResponse(authorization,
                                      createPaymentRequest,
                                      paymentServiceReferenceNumber,
                                      paymentReferenceNumber, feeResponse);
        }
    }

    private PaymentResponse getPaymentResponse(String authorization,
                                               CreatePaymentRequest createPaymentRequest,
                                               String paymentServiceReferenceNumber,
                                               String paymentReferenceNumber, FeeResponse feeResponse) {
        String caseId = createPaymentRequest.getCaseId();
        if (null != createPaymentRequest.getHwfRefNumber()) {
            log.info("resubmit/retry with help with fees for the case id: {}", caseId);
            paymentResponse = PaymentResponse.builder()
                .serviceRequestReference(paymentServiceReferenceNumber)
                .build();
            return paymentResponse;
        }

        log.info("retry for card payments, checking payment status for the case id: {} ", caseId);
        PaymentStatusResponse paymentStatus = fetchPaymentStatus(authorization, paymentReferenceNumber);
        String status = (null != paymentStatus && null != paymentStatus.getStatus()) ? paymentStatus.getStatus() : null;
        log.info("Payment Status : {} caseId: {} ", status, caseId);
        if (PAYMENT_STATUS_SUCCESS.equalsIgnoreCase(status)) {
            paymentResponse = PaymentResponse.builder()
                .paymentReference(paymentReferenceNumber)
                .serviceRequestReference(paymentServiceReferenceNumber)
                .paymentStatus(status)
                .build();

            log.info("Payment is already successful for the case id: {} ", caseId);
        } else {
            log.info("Previous payment failed, creating new payment for the caseId: {}", caseId);
            paymentResponse = createServicePayment(paymentServiceReferenceNumber, authorization,
                                                   createPaymentRequest.getReturnUrl(), feeResponse.getAmount());
            paymentResponse.setServiceRequestReference(paymentServiceReferenceNumber);
        }

        return paymentResponse;
    }

    public PaymentServiceResponse createServiceRequestFromCcdCallack(
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest, String authorisation) throws Exception {

        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        FeeResponse feeResponse = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
        return getPaymentServiceResponse(authorisation, caseData, feeResponse);
    }

    public PaymentServiceResponse getPaymentServiceResponse(String authorisation, CaseData caseData, FeeResponse feeResponse) {
        return paymentApi
            .createPaymentServiceRequest(authorisation, authTokenGenerator.generate(),
                                         PaymentServiceRequest.builder()
                                             .callBackUrl(callBackUrl)
                                             .casePaymentRequest(CasePaymentRequestDto.builder()
                                                                     .action(PAYMENT_ACTION)
                                                                     .responsibleParty(caseData.getApplicantCaseName()).build())
                                             .caseReference(String.valueOf(caseData.getId()))
                                             .ccdCaseNumber(String.valueOf(caseData.getId()))
                                             .fees(new FeeDto[]{
                                                 FeeDto.builder()
                                                     .calculatedAmount(feeResponse.getAmount())
                                                     .code(feeResponse.getCode())
                                                     .version(feeResponse.getVersion())
                                                     .volume(1).build()
                                             })
                                             .build()
            );
    }

    public PaymentServiceResponse createServiceRequestForAdditionalApplications(
        CaseData caseData, String authorisation, FeeResponse response, String serviceReferenceResponsibleParty) {
        return paymentApi
            .createPaymentServiceRequest(
                authorisation,
                authTokenGenerator.generate(),
                PaymentServiceRequest
                    .builder()
                    .callBackUrl(callBackUrl)
                    .casePaymentRequest(CasePaymentRequestDto.builder()
                                            .action(PAYMENT_ACTION)
                                            .responsibleParty(serviceReferenceResponsibleParty).build())
                    .caseReference(String.valueOf(caseData.getId()))
                    .ccdCaseNumber(String.valueOf(caseData.getId()))
                    .fees(new FeeDto[]{
                        FeeDto.builder()
                            .calculatedAmount(response.getAmount())
                            .code(response.getCode())
                            .version(response.getVersion())
                            .volume(1).build()
                    })
                    .build()
            );
    }
}
