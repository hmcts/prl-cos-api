package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.clients.PaymentApi;
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
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PAYMENT_ACTION;

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

    @Value("${payments.api.callback-url}")
    String callBackUrl;

    public PaymentServiceResponse createServiceRequest(CallbackRequest callbackRequest, String authorisation) throws Exception {
        CaseData caseData = objectMapper.convertValue(
            CaseData.builder().applicantCaseName(callbackRequest.getCaseDetails().getCaseData().getApplicantCaseName())
                .id(Long.parseLong(callbackRequest.getCaseDetails().getCaseId())).build(),
            CaseData.class
        );
        FeeResponse feeResponse = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
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

    public PaymentResponse createServicePayment(String serviceRequestReference, String authorization,
                                                String returnUrl) throws Exception {
        FeeResponse feeResponse = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
        return paymentApi
                .createPaymentRequest(serviceRequestReference, authorization, authTokenGenerator.generate(),
                        OnlineCardPaymentRequest.builder()
                                .amount(feeResponse.getAmount())
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


    public PaymentResponse createPayment(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                         @RequestHeader(SERVICE_AUTH) String serviceAuthorization,
                                         @RequestBody CreatePaymentRequest createPaymentRequest)
        throws Exception {
        //Get case using caseId
        String caseId = createPaymentRequest.getCaseId();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = coreCaseDataApi.getCase(
            authorization,
            serviceAuthorization,
            caseId
        );
        log.info("Case Data retrieved for caseId : " + caseDetails.getId().toString());
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        String paymentServiceReferenceNumber = caseData.getPaymentServiceRequestReferenceNumber();
        String paymentReferenceNumber = caseData.getC100RebuildData().getPaymentReferenceNumber();

        //Check if paymentServiceReferenceNumber and PaymentReference exist and if yes get the status of payment.
        //If status is success then return, else create payment
        if (paymentServiceReferenceNumber != null) {
            if (null != paymentReferenceNumber) {
                PaymentStatusResponse paymentStatus = fetchPaymentStatus(authorization, paymentReferenceNumber);
                String status = (null != paymentStatus && null != paymentStatus.getStatus()) ? paymentStatus.getStatus() : null;
                log.info("Payment Status : {} caseId: {} ",status, caseId);
                if (PAYMENT_STATUS_SUCCESS.equalsIgnoreCase(status)) {
                    paymentResponse = PaymentResponse.builder()
                        .paymentReference(paymentReferenceNumber)
                        .serviceRequestReference(paymentServiceReferenceNumber)
                        .paymentStatus(status)
                        .build();

                    log.info("Payment is already successful for the case id: {} ",caseId);
                    return paymentResponse;
                } else {
                    log.info("Previous payment failed, creating new payment for the caseId: {}", caseId);
                    paymentResponse = createServicePayment(paymentServiceReferenceNumber,
                                                           authorization,
                                                           createPaymentRequest.getReturnUrl());
                }
            } else {
                log.info("Creating new payment for the case id: {} ", caseId);
                paymentResponse = createServicePayment(paymentServiceReferenceNumber,
                                                       authorization,
                                                       createPaymentRequest.getReturnUrl());

                paymentResponse.setServiceRequestReference(paymentServiceReferenceNumber);
            }
        } else {
            //For help with fees we only need to create service request
            if (!isHelpWithFeesOptedInAlready(caseData)
                && null != createPaymentRequest.getHwfRefNumber()) {
                log.info("Help with fees is opted -> creating only service request for the case id: {}", caseId);
                CallbackRequest request = buildCallBackRequest(createPaymentRequest);
                PaymentServiceResponse paymentServiceResponse = createServiceRequest(request, authorization);

                paymentResponse = PaymentResponse.builder()
                    .serviceRequestReference(paymentServiceResponse.getServiceRequestReference())
                    .build();
            } else {
                // if CR and PR doesn't exist
                log.info("Creating new service request and payment request for the case id: {}", caseId);
                CallbackRequest request = buildCallBackRequest(createPaymentRequest);
                PaymentServiceResponse paymentServiceResponse = createServiceRequest(request, authorization);
                paymentResponse = createServicePayment(paymentServiceResponse.getServiceRequestReference(),
                                                       authorization, createPaymentRequest.getReturnUrl()
                );
                //set service request ref
                paymentResponse.setServiceRequestReference(paymentServiceResponse.getServiceRequestReference());
            }
        }
        return paymentResponse;
    }

    private CallbackRequest buildCallBackRequest(CreatePaymentRequest createPaymentRequest) {
        return CallbackRequest
            .builder()
            .caseDetails(CaseDetails
                             .builder()
                             .caseId(createPaymentRequest.getCaseId())
                             .caseData(CaseData
                                           .builder()
                                           .id(Long.parseLong(createPaymentRequest.getCaseId()))
                                           .applicantCaseName(createPaymentRequest.getApplicantCaseName())
                                           .build()).build())
            .build();
    }

    private boolean isHelpWithFeesOptedInAlready(CaseData caseData) {
        return null != caseData
            && isNotEmpty(caseData.getC100RebuildData().getHelpWithFeesReferenceNumber())
            && null != caseData.getPaymentServiceRequestReferenceNumber();
    }

}
