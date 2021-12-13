package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.enums.OrchestrationConstants;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.CasePaymentRequestDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentRequestService extends AbstractCallbackController {

    private final PaymentApi paymentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final FeeService feeService;

    @Value("${payments.api.callback-url}")
    String callBackURL;


    public PaymentServiceResponse createServiceRequest(CallbackRequest callbackRequest, String authorisation) throws Exception {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        FeeResponse feeResponse= feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
        PaymentServiceResponse paymentServiceResponse = paymentApi
            .createPaymentServiceRequest("eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiWjRCY2pWZ2Z2dTVaZXhLekJFRWxNU200M0xzPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJwcmxfZGVtb0BtYWlsaW5hdG9yLmNvbSIsImN0cyI6Ik9BVVRIMl9TVEFURUxFU1NfR1JBTlQiLCJhdXRoX2xldmVsIjowLCJhdWRpdFRyYWNraW5nSWQiOiJhMGQ4MzNmNS00NGZmLTRjOTAtYmQ4Zi0wY2ZmMThhZDNiYzUtMTIyMDI2NTYiLCJpc3MiOiJodHRwczovL2Zvcmdlcm9jay1hbS5zZXJ2aWNlLmNvcmUtY29tcHV0ZS1pZGFtLWRlbW8uaW50ZXJuYWw6ODQ0My9vcGVuYW0vb2F1dGgyL3JlYWxtcy9yb290L3JlYWxtcy9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6IlJvUUcwU2FtZkpLcXBpQ0J3UnFCUXBGRFYxQSIsIm5vbmNlIjoiU0psbUlSWlpOMWZlTk45Q2ZDeVVIY2VyMG9XbDNMMEFpcDVYNVVubVp6ZyIsImF1ZCI6Inh1aXdlYmFwcCIsIm5iZiI6MTYzOTM5MDI5NSwiZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyIsImNyZWF0ZS11c2VyIiwibWFuYWdlLXVzZXIiLCJzZWFyY2gtdXNlciJdLCJhdXRoX3RpbWUiOjE2MzkzOTAyOTUsInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNjM5NDE5MDk1LCJpYXQiOjE2MzkzOTAyOTUsImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiJzS1VUVEhxSEtiZVcxdm1LZUIyLUItNWFnLTAifQ.m-UdkgVY-Wc1BbNEhmDcpNIi_dOvoEQTg_Lc7PpUSqVG9DyJAoNF_IechzldqllMkXujXGFdMBQQ2T5tln3AN4q9mphzzq2pkrluuobatV0EgBiRSvxnBNVk93Yi1yjbQDJ9q6YgUS9hF2636wz2s4vPv2IEYz6jH0m3STDBl1_7pTXaV2c0EnXN5ukPmdbwaDescLipR_h9iecfEZH6cTGtzKdlF-j7_phRwltKnITVoKIU28A5d-cDxarHJCeyF6uw5_XmjjqvRgDMJx1789JX5DvLxTzI6WWwKtgBJQXmn8dqabCddsmk0PNIqgP_F9jGjoQ0XphPvb6s1d0Ieg", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJwcmxfY29zX2FwaSIsImV4cCI6MTYzOTQwNDc2NH0.iWF3bqe8A23oD2jhFLz2wHfTaToaSX664lKCtGEcxRNNtib0YMxHu532Md780eUR-1EbFCxTedx_JEMxcjz3jQ", PaymentServiceRequest.builder()
            .callBackUrl(callBackURL)
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
