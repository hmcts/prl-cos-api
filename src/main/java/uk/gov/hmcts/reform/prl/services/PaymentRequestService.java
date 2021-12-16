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
    String callBackUrl;


    public PaymentServiceResponse createServiceRequest(CallbackRequest callbackRequest, String authorisation) throws Exception {
        CaseData caseData = objectMapper.convertValue(
            CaseData.builder().applicantCaseName(callbackRequest.getCaseDetails().getCaseData().getApplicantCaseName())
            .id(Long.valueOf(callbackRequest.getCaseDetails().getCaseId())).build(),
            CaseData.class
        );
        FeeResponse feeResponse = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
        PaymentServiceResponse paymentServiceResponse = paymentApi
            .createPaymentServiceRequest("eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiWjRCY2pWZ2Z2" +
                                             "dTVaZXhLekJFRWxNU200M0xzPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJwcmxf" +
                                         "ZGVtb0BtYWlsaW5hdG9yLmNvbSIsImN0cyI6Ik9BVVRIMl9TVEFURUxFU1NfR1JBTlQi" +
                                         "LCJhdXRoX2xldmVsIjowLCJhdWRpdFRyYWNraW5nSWQiOiJhMGQ4MzNmNS00NGZmLTRj" +
                                             "OTAtYmQ4Zi0wY2ZmMThhZDNiYzUtMTIzOTQyNjgiLCJpc3MiOiJodHRwczovL2Zv" +
                                             "cmdlcm9jay1hbS5zZXJ2aWNlLmNvcmUtY29tcHV0ZS1pZGFtLWRlbW8uaW50ZXJu" +
                                         "YWw6ODQ0My9vcGVuYW0vb2F1dGgyL3JlYWxtcy",
                                         "9yb290L3JlYWxtcy9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc1" +
                                             "90b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6IlNHSmF" +
                                             "5cm9QVXdldWstazJadFl6ZE5FTGp4YyIsIm5vbmNlIjoiQUppYmhiT0JPQk5zaXRu" +
                                             "ejhpMlJWTDNlVWEtaXhUekNVLTdzZjRDMVJqWSIsImF1ZCI6Inh1aXdlYmFwcCIsI" +
                                             "m5iZiI6MTYzOTQ3ODIxOSwiZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZS" +
                                         "IsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyIsImNyZWF0ZS11c2VyIiw" +
                                             "ibWFuYWdlLXVzZXIiLCJzZWFyY2gtdXNlciJdLCJhdXRoX3RpbWUiOjE2Mzk0Nzgy" +
                                             "MTksInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNjM5NTA3MDE5LCJpYXQiOjE2Mzk0N" +
                                             "zgyMTksImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiJTTVRMNmdGT1VzWnhNUzZ3M2" +
                                             "ZrUVJjRmNQQVUifQ.KkkeYAY_PMCaLH8zhwpE82adXn70pyESVtdLOI_YWbGzMUrO" +
                                             "EZI6683ki2rvLrWJWEDyxYecHi03Dv2lmZkyvjDnxC_EVj7ao6opkAEYLUcT2GeuH" +
                                             "ZDd397tw9pyYvFum9BJisKbUoHHKnJIwJEnRc3oXrJg3brOFI5Jb3AS4tFEpgihkH" +
                                             "pJYadLWwvpOdV3MuNoi0PM8RZByGYOA1Vuh7ann4Sij7-LyR2waX2VK7DQazXL_1_" +
                                             "Pt_PEmJsz5OpLdXpX_LhyKbj3L5CxUDPnpWexyEMdg57vrzFQyNck53XKhjsl7LBW" +
                                             "nPMORWvl6L8gvJbyyCLrjRkiXG9FkqmkAQeyJhbGciOiJIUzUxMiJ9.eyJzdWIiOi" +
                                             "JwcmxfY29zX2FwaSIsImV4cCI6MTYzOTQ5MjY4OX0.J0aP7c3l0twCMN3M94G78c9" +
                                             "CiqdkQpTxRYxczgQW-fnYrqovbH3m93wqptk8N0ZFd8pQgYOn-AkaRLNApkNTkw",
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
