package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.enums.OrchestrationConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.CasePaymentRequestDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentRequestService extends AbstractCallbackController {

    private final PaymentApi paymentApi;
    private final AuthTokenGenerator authTokenGenerator;

    public PaymentServiceResponse createServiceRequest(CallbackRequest callbackRequest, String authorisation) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        PaymentServiceResponse paymentServiceResponse = paymentApi
            .createPaymentServiceRequest(authorisation,authTokenGenerator.generate(), PaymentServiceRequest.builder()
            .callBackUrl("https://manage-case.demo.platform.hmcts.net/cases")
            .casePaymentRequest(CasePaymentRequestDto.builder()
                                    .action(OrchestrationConstants.PAYMENT_ACTION)
                                    .responsibleParty(caseData.getApplicantCaseName()).build())
            .caseReference(String.valueOf(caseData.getId()))
            .ccdCaseNumber(String.valueOf(caseData.getId()))
            .fees(new FeeDto[]{
                FeeDto.builder()
                    .calculatedAmount(BigDecimal.valueOf(232))
                    .code("FEE0325")
                    .version(OrchestrationConstants.FEE_VERSION)
                    .volume(OrchestrationConstants.FEE_VOLUME).build()
            })
            .build());
        return  paymentServiceResponse;
    }
}
