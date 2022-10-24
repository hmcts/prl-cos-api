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

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_UPDATE_REFERENCE;
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
    private CaseService caseService;
    private static final String SERVICE_AUTH = "ServiceAuthorization";
    private static final String PAYMENTSTATUS = "Success";
    private PaymentResponse paymentResponse;

    @Value("${payments.api.callback-url}")
    String callBackUrl;

    public PaymentServiceResponse createServiceRequest(CallbackRequest callbackRequest, String authorisation) throws Exception {
        CaseData caseData = objectMapper.convertValue(
            CaseData.builder().applicantCaseName(callbackRequest.getCaseDetails().getCaseData().getApplicantCaseName())
                .id(Long.valueOf(callbackRequest.getCaseDetails().getCaseId())).build(),
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
                                                    String paymentReference) throws Exception {
        return paymentApi
            .fetchPaymentStatus(authorization, authTokenGenerator.generate(),
                                paymentReference
            );
    }


    public PaymentResponse createPayment(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                         @RequestHeader(SERVICE_AUTH) String serviceAuthorization,
                                         @RequestBody CreatePaymentRequest createPaymentRequest)
        throws Exception {

        //Get case using Caseid
        String caseId = createPaymentRequest.getCaseId();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = coreCaseDataApi.getCase(
            authorization,
            serviceAuthorization,
            caseId
        );
        log.info("Case Data retrieved for id : " + caseDetails.getId().toString());
        CaseData tempCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        String paymentServiceReferenceNumber = tempCaseData.getPaymentServiceRequestReferenceNumber();
        String paymentReferenceNumber = tempCaseData.getPaymentReferenceNumber();
        log.info("paymentServiceReferenceNumber : {}, paymentReferenceNumber :{} for the case id: {} ",
            paymentServiceReferenceNumber,paymentReferenceNumber,caseId);
        //Check if paymentServiceReferenceNumber and PaymentReference exist and if yes get the status of payment.
        //If its success then payment is success else create payment
        if (paymentServiceReferenceNumber != null && paymentReferenceNumber != null) {
            PaymentStatusResponse paymentStatus = fetchPaymentStatus(authorization, paymentServiceReferenceNumber);
            if (paymentStatus.getStatus() != null && paymentStatus.getStatus() != PAYMENTSTATUS) {
                log.info("Payment Status :{} for the case id: {} ",paymentStatus.getStatus(),caseId);
                paymentResponse = createServicePayment(paymentServiceReferenceNumber,
                                                                       authorization,
                                                                       createPaymentRequest.getReturnUrl());
                log.info("Payments made for the case id: {} ",caseId);
                updateReferenceNumber(paymentServiceReferenceNumber,
                                      authorization,serviceAuthorization,caseId);
                log.info("Updated the case data for the case id :{}",caseId);

            }
            //Check if paymentServiceReferenceNumber exists and PaymentReference is null
        } else if (paymentServiceReferenceNumber != null && paymentReferenceNumber == null) {
            paymentResponse = createServicePayment(paymentServiceReferenceNumber,
                                                   authorization,
                                                   createPaymentRequest.getReturnUrl());
            log.info("Payments made for the case id: {} ",caseId);
            updateReferenceNumber(paymentServiceReferenceNumber,
                                  authorization,serviceAuthorization,caseId);
            log.info("Updated the case data for the case id :{}",caseId);

        } else {
            // if CR and PR doesnt exist
            CallbackRequest request = buildCallBackRequest(createPaymentRequest);
            PaymentServiceResponse paymentServiceResponse = createServiceRequest(request, authorization);
            paymentResponse = createServicePayment(paymentServiceResponse.getServiceRequestReference(),
                                                                   authorization, createPaymentRequest.getReturnUrl()
            );
            log.info("Payments made for the case id: {} ",caseId);
            paymentServiceReferenceNumber = paymentServiceResponse.getServiceRequestReference();
            updateReferenceNumber(paymentServiceReferenceNumber,
                                  authorization,serviceAuthorization,caseId);
            log.info("Updated the case data for the case id :{} ",caseId);

        }
        return paymentResponse;
    }


    private void  updateReferenceNumber(String paymentServiceReferenceNumber, String authorization,
                                         String serviceAuthorization, String caseId) {
        CaseData caseData = objectMapper.convertValue(
            CaseData.builder()
                .paymentServiceRequestReferenceNumber(paymentServiceReferenceNumber)
                .paymentReferenceNumber(paymentResponse.getPaymentReference()).build(),
            CaseData.class
        );
        caseService.updateCase(caseData,
                               authorization,
                               serviceAuthorization,
                               caseId,
                               CITIZEN_UPDATE_REFERENCE);
        log.info("Updated the case data for the case id :{}",caseId);
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
}
