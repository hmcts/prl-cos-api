package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CcdPayment;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CcdPaymentServiceRequestUpdate;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;

import java.time.LocalDateTime;
import java.util.Objects;

import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.JURISDICTION;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RequestUpdateCallbackService {

    public static final String PAYMENT_SUCCESS_CALLBACK = "paymentSuccessCallback";
    public static final String PAYMENT_FAILURE_CALLBACK = "paymentFailureCallback";
    public static final String PAID = "Paid";
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final CoreCaseDataApi coreCaseDataApi;
    private final SystemUserService systemUserService;
    private final SolicitorEmailService solicitorEmailService;
    private final CaseWorkerEmailService caseWorkerEmailService;
    private final UserService userService;

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto) throws Exception {

        log.info("Processing the callback for the caseId {} with status {}", serviceRequestUpdateDto.getCcdCaseNumber(),
                 serviceRequestUpdateDto.getServiceRequestStatus()
        );
        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);
        log.info("Fetching the Case details based on caseId {}", serviceRequestUpdateDto.getCcdCaseNumber()
        );
        CaseDetails caseDetails = coreCaseDataApi.getCase(
            userToken,
            authTokenGenerator.generate(),
            serviceRequestUpdateDto.getCcdCaseNumber()
        );

        if (!Objects.isNull(caseDetails.getId())) {
            log.info(
                "Updating the Case data with payment information for caseId {}",
                serviceRequestUpdateDto.getCcdCaseNumber()
            );
            createEvent(serviceRequestUpdateDto, userToken, systemUpdateUserId,
                        serviceRequestUpdateDto.getServiceRequestStatus().equalsIgnoreCase(PAID)
                            ? PAYMENT_SUCCESS_CALLBACK : PAYMENT_FAILURE_CALLBACK
            );

            solicitorEmailService.sendEmail(caseDetails);
            caseWorkerEmailService.sendEmail(caseDetails);

        } else {
            log.error("Case id {} not present", serviceRequestUpdateDto.getCcdCaseNumber());
            throw new Exception("Case not present");
        }
    }

    //todo This method will be deleted once we wipe out Fee and Pay Bypass
    public void processCallbackForBypass(ServiceRequestUpdateDto serviceRequestUpdateDto, String authorisation) throws Exception {

        log.info("Processing the callback for the caseId {} with status {}", serviceRequestUpdateDto.getCcdCaseNumber(),
                 serviceRequestUpdateDto.getServiceRequestStatus()
        );
        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);
        log.info("Fetching the Case details based on caseId {}", serviceRequestUpdateDto.getCcdCaseNumber()
        );
        CaseDetails caseDetails = coreCaseDataApi.getCase(
            userToken,
            authTokenGenerator.generate(),
            serviceRequestUpdateDto.getCcdCaseNumber()
        );

        if (!Objects.isNull(caseDetails.getId())) {
            log.info(
                "Updating the Case data with payment information for caseId {}",
                serviceRequestUpdateDto.getCcdCaseNumber()
            );
            createEventForFeeAndPayBypass(serviceRequestUpdateDto, userToken, systemUpdateUserId,
                                          serviceRequestUpdateDto.getServiceRequestStatus().equalsIgnoreCase(PAID)
                                              ? PAYMENT_SUCCESS_CALLBACK : PAYMENT_FAILURE_CALLBACK,
                                          authorisation
            );

            solicitorEmailService.sendEmail(caseDetails);
            caseWorkerEmailService.sendEmail(caseDetails);

        } else {
            log.error("Case id {} not present", serviceRequestUpdateDto.getCcdCaseNumber());
            throw new Exception("Case not present");
        }
    }

    // todo this method will be deleted once we wipe out fee and pay bypass
    private void createEventForFeeAndPayBypass(ServiceRequestUpdateDto serviceRequestUpdateDto, String userToken,
                                               String systemUpdateUserId, String eventId, String authorisation) {
        CaseData caseData = setCaseData(serviceRequestUpdateDto);
        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE,
            serviceRequestUpdateDto.getCcdCaseNumber(),
            eventId
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

    private void createEvent(ServiceRequestUpdateDto serviceRequestUpdateDto, String userToken,
                             String systemUpdateUserId, String eventId) {
        CaseData caseData = setCaseData(serviceRequestUpdateDto);
        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE,
            serviceRequestUpdateDto.getCcdCaseNumber(),
            eventId
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


    //todo this method will be deleted once we wipe out fee and pay bypass
    private CaseData setCaseDataFeeAndPayBypass(ServiceRequestUpdateDto serviceRequestUpdateDto, String authorisation) {
        return objectMapper.convertValue(
            CaseData.builder()
                .id(Long.valueOf(serviceRequestUpdateDto.getCcdCaseNumber()))
                .applicantSolicitorEmailAddress(userService.getUserDetails(authorisation).getEmail())
                .caseworkerEmailAddress("prl_caseworker_solicitor@mailinator.com")
                .paymentCallbackServiceRequestUpdate(CcdPaymentServiceRequestUpdate.builder()
                                                         .serviceRequestReference(serviceRequestUpdateDto.getServiceRequestReference())
                                                         .ccdCaseNumber(serviceRequestUpdateDto.getCcdCaseNumber())
                                                         .serviceRequestAmount(serviceRequestUpdateDto.getServiceRequestAmount())
                                                         .serviceRequestStatus(serviceRequestUpdateDto.getServiceRequestStatus())
                                                         .callBackUpdateTimestamp(LocalDateTime.now())
                                                         .payment(CcdPayment.builder().paymentAmount(
                                                             serviceRequestUpdateDto.getPayment().getPaymentAmount())
                                                                      .paymentReference(serviceRequestUpdateDto.getPayment().getPaymentReference())
                                                                      .paymentMethod(serviceRequestUpdateDto.getPayment().getPaymentMethod())
                                                                      .caseReference(serviceRequestUpdateDto.getPayment().getCaseReference())
                                                                      .accountNumber(serviceRequestUpdateDto.getPayment().getAccountNumber())
                                                                      .build()).build()).build(),
            CaseData.class
        );

    }

    private CaseData setCaseData(ServiceRequestUpdateDto serviceRequestUpdateDto) {
        return objectMapper.convertValue(
            CaseData.builder()
                .id(Long.valueOf(serviceRequestUpdateDto.getCcdCaseNumber()))
                .paymentCallbackServiceRequestUpdate(CcdPaymentServiceRequestUpdate.builder()
                                                         .serviceRequestReference(serviceRequestUpdateDto.getServiceRequestReference())
                                                         .ccdCaseNumber(serviceRequestUpdateDto.getCcdCaseNumber())
                                                         .serviceRequestAmount(serviceRequestUpdateDto.getServiceRequestAmount())
                                                         .serviceRequestStatus(serviceRequestUpdateDto.getServiceRequestStatus())
                                                         .callBackUpdateTimestamp(LocalDateTime.now())
                                                         .payment(CcdPayment.builder().paymentAmount(
                                                             serviceRequestUpdateDto.getPayment().getPaymentAmount())
                                                                      .paymentReference(serviceRequestUpdateDto.getPayment().getPaymentReference())
                                                                      .paymentMethod(serviceRequestUpdateDto.getPayment().getPaymentMethod())
                                                                      .caseReference(serviceRequestUpdateDto.getPayment().getCaseReference())
                                                                      .accountNumber(serviceRequestUpdateDto.getPayment().getAccountNumber())
                                                                      .build()).build()).build(),
            CaseData.class
        );

    }


}
