package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.CCDPayment;
import uk.gov.hmcts.reform.prl.models.dto.payment.CCDPaymentServiceRequestUpdate;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.JURISDICTION;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RequestUpdateCallbackService {

    public static final String PAYMENT_CALLBACK = "paymentCallback";
    public static final String PAID = "Paid";
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final CoreCaseDataApi coreCaseDataApi;
    private final SystemUserService systemUserService;

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto) {


        if (serviceRequestUpdateDto.getServiceRequestStatus().equalsIgnoreCase(PAID)) {
            CaseData caseData = setCaseData(serviceRequestUpdateDto);

            String userToken = systemUserService.getSysUserToken();
            String systemUpdateUserId = systemUserService.getUserId(userToken);
            StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
                userToken,
                authTokenGenerator.generate(),
                systemUpdateUserId,
                JURISDICTION,
                CASE_TYPE,
                serviceRequestUpdateDto.getCcdCaseNumber(),
                PAYMENT_CALLBACK
            );

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                           .id(startEventResponse.getEventId())
                           .build())
                .data(caseData)
                .build();

            coreCaseDataApi.submitEventForCaseWorker(
                userToken,
                authTokenGenerator.generate(),
                systemUpdateUserId,
                JURISDICTION,
                CASE_TYPE,
                serviceRequestUpdateDto.getCcdCaseNumber(),
                true,
                caseDataContent
            );
        }
    }

    private CaseData setCaseData(ServiceRequestUpdateDto serviceRequestUpdateDto) {
        return objectMapper.convertValue(CaseData.builder()
                                             .id(Long.valueOf(serviceRequestUpdateDto.getCcdCaseNumber()))
                                             .paymentCallbackServiceRequestUpdate(
                                                 CCDPaymentServiceRequestUpdate.builder()
                                                     .serviceRequestReference(
                                                         serviceRequestUpdateDto.getServiceRequestReference())
                                                     .ccdCaseNumber(serviceRequestUpdateDto.getCcdCaseNumber())
                                                     .serviceRequestAmount(serviceRequestUpdateDto.getServiceRequestAmount())
                                                     .serviceRequestStatus(serviceRequestUpdateDto.getServiceRequestStatus())
                                                     .callBackUpdateTimestamp(LocalDateTime.now())
                                                     .payment(CCDPayment.builder().paymentAmount(
                                                         serviceRequestUpdateDto.getPayment().getPaymentAmount())
                                                                  .paymentReference(serviceRequestUpdateDto.getPayment().getPaymentReference())
                                                                  .paymentMethod(serviceRequestUpdateDto.getPayment().getPaymentMethod())
                                                                  .caseReference(serviceRequestUpdateDto.getPayment().getCaseReference())
                                                                  .accountNumber(serviceRequestUpdateDto.getPayment().getAccountNumber()).build()).build())
                                             .build(), CaseData.class);
    }
}
