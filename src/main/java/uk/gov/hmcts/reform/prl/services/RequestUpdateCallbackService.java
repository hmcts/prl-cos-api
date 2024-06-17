package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.UploadAdditionalApplicationUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_WA_TASK_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_WA_TASK_TO_BE_CREATED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;

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
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    private final UploadAdditionalApplicationUtils uploadAdditionalApplicationUtils;

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto) {
        String systemAuthorisation = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(systemAuthorisation);
        CaseDetails caseDetails
            = coreCaseDataService.findCaseById(systemAuthorisation, serviceRequestUpdateDto.getCcdCaseNumber());

        boolean isCasePayment = verifyCaseCreationPaymentReference(
            caseDetails,
            serviceRequestUpdateDto.getServiceRequestReference()
        );
        CaseEvent caseEvent;
        if (isCasePayment) {
            caseEvent = PAID.equalsIgnoreCase(serviceRequestUpdateDto.getServiceRequestStatus())
                ? CaseEvent.PAYMENT_SUCCESS_CALLBACK : CaseEvent.PAYMENT_FAILURE_CALLBACK;
        } else {
            caseEvent = PAID.equalsIgnoreCase(serviceRequestUpdateDto.getServiceRequestStatus())
                ? CaseEvent.AWP_PAYMENT_SUCCESS_CALLBACK : CaseEvent.AWP_PAYMENT_FAILURE_CALLBACK;
        }

        log.info("Following case event will be triggered {}", caseEvent.getValue());

        EventRequestData eventRequestData = coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId);
        StartEventResponse startEventResponse =
            coreCaseDataService.startUpdate(
                systemAuthorisation,
                eventRequestData,
                serviceRequestUpdateDto.getCcdCaseNumber(),
                true
            );

        CaseDataContent caseDataContent;
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
            systemAuthorisation,
            eventRequestData,
            caseDataContent,
            serviceRequestUpdateDto.getCcdCaseNumber(),
            true
        );

        partyLevelCaseFlagsService.generateAndStoreCaseFlags(serviceRequestUpdateDto.getCcdCaseNumber());

        if (isCasePayment) {
            EventRequestData allTabsUpdateEventRequestData = coreCaseDataService.eventRequest(
                CaseEvent.UPDATE_ALL_TABS,
                systemUpdateUserId
            );
            StartEventResponse allTabsUpdateStartEventResponse =
                coreCaseDataService.startUpdate(
                    systemAuthorisation,
                    allTabsUpdateEventRequestData,
                    serviceRequestUpdateDto.getCcdCaseNumber(),
                    true
                );

            CaseData allTabsUpdateCaseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(
                allTabsUpdateStartEventResponse,
                objectMapper
            );
            log.info(
                "Refreshing tab based on the payment response for caseId {} ",
                serviceRequestUpdateDto.getCcdCaseNumber()
            );

            allTabsUpdateCaseData = getCaseDataWithStateAndDateSubmitted(
                serviceRequestUpdateDto,
                allTabsUpdateCaseData
            );
            log.info("*** court code from fact  {}", allTabsUpdateCaseData.getCourtCodeFromFact());

            allTabService.mapAndSubmitAllTabsUpdate(
                systemAuthorisation,
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

    private boolean verifyCaseCreationPaymentReference(CaseDetails caseDetails, String serviceRequestReference) {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        return !StringUtils.isEmpty(serviceRequestReference)
                && serviceRequestReference.equalsIgnoreCase(caseData.getPaymentServiceRequestReferenceNumber());
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
            log.error("Error while populating case date in payment request call {}", caseData.getId(), e);
        }
        return caseData;
    }

    private CaseData setCaseData(ServiceRequestUpdateDto serviceRequestUpdateDto) {
        return CaseData.builder()
            .id(Long.parseLong(serviceRequestUpdateDto.getCcdCaseNumber()))
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

    private Map<String, Object> setAwPPaymentCaseData(StartEventResponse startEventResponse, ServiceRequestUpdateDto serviceRequestUpdateDto) {
        CaseData startEventResponseData = CaseUtils.getCaseData(startEventResponse.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = new HashMap<>();
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
                caseDataUpdated.put("additionalApplicationsBundle", startEventResponseData.getAdditionalApplicationsBundle());
                caseDataUpdated.put(
                    AWP_WA_TASK_NAME,
                    uploadAdditionalApplicationUtils.getAwPTaskNameWhenPaymentCompleted(updatedAdditionalApplicationsBundleElement));
                caseDataUpdated.put(AWP_WA_TASK_TO_BE_CREATED, YES);
            }
        }
        return caseDataUpdated;
    }
}
