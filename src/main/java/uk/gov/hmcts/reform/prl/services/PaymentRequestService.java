package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
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
    String callBackURL;


    public PaymentServiceResponse createServiceRequest(CallbackRequest callbackRequest, String authorisation) throws Exception {
        CaseData caseData = objectMapper.convertValue(
            CaseData.builder().applicantCaseName(callbackRequest.getCaseDetails().getCaseData().getApplicantCaseName())
            .id(Long.valueOf(callbackRequest.getCaseDetails().getCaseId())).build(),
            CaseData.class
        );
        FeeResponse feeResponse= feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
        PaymentServiceResponse paymentServiceResponse = paymentApi
            .createPaymentServiceRequest("eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiWjRCY2pWZ2Z2dTVaZXhLekJFRWxNU200M0xzPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJwcmxfZGVtb0BtYWlsaW5hdG9yLmNvbSIsImN0cyI6Ik9BVVRIMl9TVEFURUxFU1NfR1JBTlQiLCJhdXRoX2xldmVsIjowLCJhdWRpdFRyYWNraW5nSWQiOiJhMGQ4MzNmNS00NGZmLTRjOTAtYmQ4Zi0wY2ZmMThhZDNiYzUtMTIzOTQyNjgiLCJpc3MiOiJodHRwczovL2Zvcmdlcm9jay1hbS5zZXJ2aWNlLmNvcmUtY29tcHV0ZS1pZGFtLWRlbW8uaW50ZXJuYWw6ODQ0My9vcGVuYW0vb2F1dGgyL3JlYWxtcy9yb290L3JlYWxtcy9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6IlNHSmF5cm9QVXdldWstazJadFl6ZE5FTGp4YyIsIm5vbmNlIjoiQUppYmhiT0JPQk5zaXRuejhpMlJWTDNlVWEtaXhUekNVLTdzZjRDMVJqWSIsImF1ZCI6Inh1aXdlYmFwcCIsIm5iZiI6MTYzOTQ3ODIxOSwiZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyIsImNyZWF0ZS11c2VyIiwibWFuYWdlLXVzZXIiLCJzZWFyY2gtdXNlciJdLCJhdXRoX3RpbWUiOjE2Mzk0NzgyMTksInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNjM5NTA3MDE5LCJpYXQiOjE2Mzk0NzgyMTksImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiJTTVRMNmdGT1VzWnhNUzZ3M2ZrUVJjRmNQQVUifQ.KkkeYAY_PMCaLH8zhwpE82adXn70pyESVtdLOI_YWbGzMUrOEZI6683ki2rvLrWJWEDyxYecHi03Dv2lmZkyvjDnxC_EVj7ao6opkAEYLUcT2GeuHZDd397tw9pyYvFum9BJisKbUoHHKnJIwJEnRc3oXrJg3brOFI5Jb3AS4tFEpgihkHpJYadLWwvpOdV3MuNoi0PM8RZByGYOA1Vuh7ann4Sij7-LyR2waX2VK7DQazXL_1_Pt_PEmJsz5OpLdXpX_LhyKbj3L5CxUDPnpWexyEMdg57vrzFQyNck53XKhjsl7LBWnPMORWvl6L8gvJbyyCLrjRkiXG9FkqmkAQ", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJwcmxfY29zX2FwaSIsImV4cCI6MTYzOTQ5MjY4OX0.J0aP7c3l0twCMN3M94G78c9CiqdkQpTxRYxczgQW-fnYrqovbH3m93wqptk8N0ZFd8pQgYOn-AkaRLNApkNTkw", PaymentServiceRequest.builder()
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
