package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.PaymentApi;
import uk.gov.hmcts.reform.prl.enums.OrchestrationConstants;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.CasePaymentRequestDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;

@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentRequestService {


    private final PaymentApi paymentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final FeeService feeService;
    private final ObjectMapper objectMapper;

    @Value("${payments.api.callback-url}")
    String callBackUrl;


    public PaymentServiceResponse createServiceRequest(CallbackRequest callbackRequest, String authorisation) throws Exception {
        CaseData caseData = objectMapper.convertValue(
            CaseData.builder().applicantCaseName(callbackRequest.getCaseDetails().getCaseData().getApplicantCaseName())
            .id(Long.valueOf(callbackRequest.getCaseDetails().getCaseId())).build(),
            CaseData.class
        );
        FeeResponse feeResponse = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
        PaymentServiceResponse paymentServiceResponse = paymentApi
            .createPaymentServiceRequest(authorisation,authTokenGenerator.generate(),
                                         PaymentServiceRequest.builder()
            .callBackUrl(callBackUrl)
            .casePaymentRequest(CasePaymentRequestDto.builder()
                                    .action(OrchestrationConstants.PAYMENT_ACTION)
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
            .build());
        return  paymentServiceResponse;
    }
}
