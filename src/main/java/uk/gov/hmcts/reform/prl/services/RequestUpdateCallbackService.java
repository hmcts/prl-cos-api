package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.ApplicationStatus;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.PaymentStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CcdPayment;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CcdPaymentServiceRequestUpdate;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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

        log.info("Processing call back with new AwP changes");

        log.info(
            "Processing the callback for the service reference number {} caseId {} with status {}",
            serviceRequestUpdateDto.getServiceRequestReference(),
            serviceRequestUpdateDto.getCcdCaseNumber(),
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

        CaseDataContent caseDataContent = null;
        boolean isCasePayment = verifyCaseCreationPaymentReference(
            startEventResponse,
            serviceRequestUpdateDto.getServiceRequestReference()
        );
        if (isCasePayment) {
            caseDataContent = coreCaseDataService.createCaseDataContent(
                startEventResponse,
                setCaseData(
                    serviceRequestUpdateDto
                )
            );
        } else {
            caseDataContent = coreCaseDataService.createCaseDataContent(
                startEventResponse,
                setAwPPaymentCaseData(
                    startEventResponse,
                    serviceRequestUpdateDto
                )
            );
        }

        coreCaseDataService.submitUpdate(
            authorisation,
            eventRequestData,
            caseDataContent,
            serviceRequestUpdateDto.getCcdCaseNumber(),
            true
        );

        if (isCasePayment) {
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

            allTabsUpdateCaseData = getCaseDataWithStateAndDateSubmitted(
                serviceRequestUpdateDto,
                allTabsUpdateCaseData
            );
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
    }

    private boolean verifyCaseCreationPaymentReference(StartEventResponse startEventResponse, String serviceRequestReference) {
        CaseData startEventResponseData = CaseUtils.getCaseData(startEventResponse.getCaseDetails(), objectMapper);
        boolean isCasePayment = false;
        if (!StringUtils.isEmpty(serviceRequestReference)
            && serviceRequestReference.equalsIgnoreCase(startEventResponseData.getPaymentServiceRequestReferenceNumber())) {
            isCasePayment = true;
        }
        return isCasePayment;
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

    private CaseData setAwPPaymentCaseData(StartEventResponse startEventResponse, ServiceRequestUpdateDto serviceRequestUpdateDto) {
        CaseData startEventResponseData = CaseUtils.getCaseData(startEventResponse.getCaseDetails(), objectMapper);
        if (startEventResponseData.getAdditionalApplicationsBundle() != null) {
            Optional<Element<AdditionalApplicationsBundle>> additionalApplicationsBundleElement
                = startEventResponseData.getAdditionalApplicationsBundle()
                .stream()
                .filter(x -> null != x.getValue().getPayment()
                    && x.getValue().getPayment().getPaymentServiceRequestReferenceNumber().equalsIgnoreCase(
                    serviceRequestUpdateDto.getServiceRequestReference()))
                .findFirst();

            if (additionalApplicationsBundleElement.isPresent()
                && PAID.equalsIgnoreCase(serviceRequestUpdateDto.getServiceRequestStatus())) {
                AdditionalApplicationsBundle updatedAdditionalApplicationsBundleElement = additionalApplicationsBundleElement.get()
                    .getValue()
                    .toBuilder()
                    .payment(additionalApplicationsBundleElement.get().getValue().getPayment().toBuilder()
                                 .status(PaymentStatus.PAID.getDisplayedValue())
                                 .build())
                    .c2DocumentBundle(null != additionalApplicationsBundleElement.get().getValue().getC2DocumentBundle()
                                      ? additionalApplicationsBundleElement.get().getValue().getC2DocumentBundle().toBuilder().applicationStatus(
                        ApplicationStatus.SUBMITTED.getDisplayedValue()).build() : null)
                    .otherApplicationsBundle(null != additionalApplicationsBundleElement.get().getValue().getOtherApplicationsBundle()
                                             ? additionalApplicationsBundleElement.get().getValue().getOtherApplicationsBundle()
                                                 .toBuilder().applicationStatus(ApplicationStatus.SUBMITTED.getDisplayedValue()).build() : null)
                    .build();

                int index = startEventResponseData.getAdditionalApplicationsBundle().indexOf(
                    additionalApplicationsBundleElement.get());
                if (index != -1) {
                    startEventResponseData.getAdditionalApplicationsBundle()
                        .set(
                            index,
                            ElementUtils.element(
                                additionalApplicationsBundleElement.get().getId(),
                                updatedAdditionalApplicationsBundleElement
                            )
                        );
                }
            }
        }
        return startEventResponseData;
    }
}
