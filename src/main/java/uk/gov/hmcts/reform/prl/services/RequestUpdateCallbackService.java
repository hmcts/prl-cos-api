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
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CcdPayment;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CcdPaymentServiceRequestUpdate;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

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
    private final AllTabServiceImpl allTabService;
    private final C100JsonMapper c100JsonMapper;
    private final CourtFinderService courtFinderService;

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto) {

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
            CaseData caseData = objectMapper.convertValue(
                caseDetails.getData(),
                CaseData.class
            );
            log.info(
                "Refreshing tab based on the payment response for caseid {} ",
                serviceRequestUpdateDto.getCcdCaseNumber()
            );
            caseData = getCaseDataWithStateAndDateSubmitted(serviceRequestUpdateDto, caseData);
            allTabService.updateAllTabsIncludingConfTab(caseData);

            log.info(
                "Refreshed the tab for caseid {} ",
                serviceRequestUpdateDto.getCcdCaseNumber()
            );

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
            throw new CaseNotFoundException("Case not present");
        }
    }

    private CaseData getCaseDataWithStateAndDateSubmitted(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                          CaseData caseData) {
        try {
            Court closestChildArrangementsCourt = courtFinderService.getNearestFamilyCourt(caseData);
            if (serviceRequestUpdateDto.getServiceRequestStatus().equalsIgnoreCase(PAID)) {
                ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
                caseData = caseData.toBuilder()
                    .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
                    .state(State.SUBMITTED_PAID)
                    .build();

            } else {
                caseData = caseData.toBuilder()
                    .state(State.SUBMITTED_NOT_PAID)
                    .build();
            }
            caseData = caseData.toBuilder().courtName(closestChildArrangementsCourt != null
                                               ? closestChildArrangementsCourt.getCourtName() : "").build();
        } catch (Exception e) {
            log.info("Error while populating case date in payment request call {}", caseData.getId());
        }
        return caseData;
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
