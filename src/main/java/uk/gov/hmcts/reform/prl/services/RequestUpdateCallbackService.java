package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CcdPayment;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CcdPaymentServiceRequestUpdate;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RequestUpdateCallbackService {

    public static final String PAID = "Paid";
    private final ObjectMapper objectMapper;
    private final SystemUserService systemUserService;
    private final SolicitorEmailService solicitorEmailService;
    private final CaseWorkerEmailService caseWorkerEmailService;
    private final AllTabServiceImpl allTabService;
    private final CourtFinderService courtFinderService;
    private final CcdCoreCaseDataService coreCaseDataService;

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto) {

        log.info("Processing the callback for the caseId {} with status {}", serviceRequestUpdateDto.getCcdCaseNumber(),
                 serviceRequestUpdateDto.getServiceRequestStatus()
        );
        String authorisation = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(authorisation);
        log.info("Starting update processing for caseId {}", serviceRequestUpdateDto.getCcdCaseNumber());

        CaseEvent caseEvent = PAID.equalsIgnoreCase(serviceRequestUpdateDto.getServiceRequestStatus())
            ? CaseEvent.PAYMENT_SUCCESS_CALLBACK : CaseEvent.PAYMENT_FAILURE_CALLBACK;

        log.info("Following case event will be triggered {}", caseEvent.getValue());

        EventRequestData eventRequestData = coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId);
        StartEventResponse startEventResponse =
            coreCaseDataService.startUpdate(
                authorisation,
                eventRequestData,
                serviceRequestUpdateDto.getCcdCaseNumber(),
                true
            );

        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContent(
            startEventResponse,
            setCaseData(
                serviceRequestUpdateDto
            )
        );

        coreCaseDataService.submitUpdate(
            authorisation,
            eventRequestData,
            caseDataContent,
            serviceRequestUpdateDto.getCcdCaseNumber(),
            true
        );

        EventRequestData allTabsUpdateEventRequestData = coreCaseDataService.eventRequest(
            CaseEvent.UPDATE_ALL_TABS,
            systemUpdateUserId
        );
        StartEventResponse allTabsUpdateStartEventResponse =
            coreCaseDataService.startUpdate(
                authorisation,
                allTabsUpdateEventRequestData,
                serviceRequestUpdateDto.getCcdCaseNumber(),
                true
            );

        CaseData allTabsUpdateCaseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(
            allTabsUpdateStartEventResponse,
            objectMapper
        );
        log.info(
            "Refreshing tab based on the payment response for caseid {} ",
            serviceRequestUpdateDto.getCcdCaseNumber()
        );

        allTabsUpdateCaseData = getCaseDataWithStateAndDateSubmitted(serviceRequestUpdateDto, allTabsUpdateCaseData);
        log.info("*** court code from fact  {}", allTabsUpdateCaseData.getCourtCodeFromFact());

        allTabService.updateAllTabsIncludingConfTabRefactored(
            authorisation,
            serviceRequestUpdateDto.getCcdCaseNumber(),
            allTabsUpdateStartEventResponse,
            allTabsUpdateEventRequestData,
            allTabsUpdateCaseData
        );

        log.info(
            "Updating the Case data with payment information for caseId {}",
            serviceRequestUpdateDto.getCcdCaseNumber()
        );

        if (PAID.equalsIgnoreCase(serviceRequestUpdateDto.getServiceRequestStatus())) {
            solicitorEmailService.sendEmail(allTabsUpdateStartEventResponse.getCaseDetails());
            caseWorkerEmailService.sendEmail(allTabsUpdateStartEventResponse.getCaseDetails());
        }
    }

    private CaseData getCaseDataWithStateAndDateSubmitted(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                          CaseData caseData) {
        try {
            Court closestChildArrangementsCourt = courtFinderService.getNearestFamilyCourt(caseData);
            if (serviceRequestUpdateDto.getServiceRequestStatus().equalsIgnoreCase(PAID)) {
                ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
                caseData = caseData.toBuilder()
                    .state(State.SUBMITTED_PAID)
                    .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
                    .build();

            } else {
                caseData = caseData.toBuilder()
                    .state(State.SUBMITTED_NOT_PAID)
                    .build();
            }
            if (closestChildArrangementsCourt != null) {
                caseData = caseData.toBuilder().courtName(closestChildArrangementsCourt.getCourtName()).courtId(String.valueOf(
                    closestChildArrangementsCourt.getCountyLocationCode())).build();
                caseData.setCourtCodeFromFact(String.valueOf(closestChildArrangementsCourt.getCountyLocationCode()));
            }
        } catch (Exception e) {
            log.error("Error while populating case date in payment request call {}", caseData.getId());
        }
        return caseData;
    }

    private CaseData setCaseData(ServiceRequestUpdateDto serviceRequestUpdateDto) {
        return CaseData.builder()
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
                                                                  .build()).build()).build();
    }
}
