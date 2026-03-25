package uk.gov.hmcts.reform.prl.services.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_ADDITIONAL_APPLICATION_COLLECTION_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentAsyncService {
    public static final String PAID = "Paid";
    private final SolicitorEmailService solicitorEmailService;
    private final CaseWorkerEmailService caseWorkerEmailService;
    private final AllTabServiceImpl allTabService;
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    private final CcdCoreCaseDataService coreCaseDataService;
    private final ObjectMapper objectMapper;
    private final CourtFinderService courtFinderService;
    private final UploadAdditionalApplicationUtils uploadAdditionalApplicationUtils;
    private final SystemUserService systemUserService;

    @Async("taskExecutor")
    public void handlePaymentCallback(ServiceRequestUpdateDto serviceRequestUpdateDto) {
        String systemAuthorisation = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(systemAuthorisation);
        log.info("Async processing started for case {}", serviceRequestUpdateDto.getCcdCaseNumber());

        // 1. Fetch latest case data (Heavy Lifting moved here)
        CaseDetails caseDetails = coreCaseDataService.findCaseById(systemAuthorisation, serviceRequestUpdateDto.getCcdCaseNumber());
        CaseData currentCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        // 2. Idempotency Check
        if (isAlreadyProcessed(currentCaseData, serviceRequestUpdateDto)) {
            log.info("Payment reference {} already processed for Case {}. Skipping.",
                     serviceRequestUpdateDto.getServiceRequestReference(), caseDetails.getId());
            return;
        }

        // 3. Sync Payment Metadata (Logs 'Payment Confirmation' and updates AWP collections)
        syncPaymentMetadata(serviceRequestUpdateDto, systemAuthorisation, systemUpdateUserId, currentCaseData);

        // 4. Update Case Flags and Refresh All Tabs
        partyLevelCaseFlagsService.generateAndStoreCaseFlags(serviceRequestUpdateDto.getCcdCaseNumber());

        EventRequestData allTabsUpdateEventRequestData = coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, systemUpdateUserId);
        StartEventResponse allTabsUpdateStartEventResponse = coreCaseDataService.startUpdate(
            systemAuthorisation, allTabsUpdateEventRequestData, serviceRequestUpdateDto.getCcdCaseNumber(), true);

        CaseData allTabsUpdateCaseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(allTabsUpdateStartEventResponse, objectMapper);

        allTabsUpdateCaseData = getCaseDataWithStateAndDateSubmitted(serviceRequestUpdateDto, allTabsUpdateCaseData);

        allTabService.mapAndSubmitAllTabsUpdate(
            systemAuthorisation, serviceRequestUpdateDto.getCcdCaseNumber(),
            allTabsUpdateStartEventResponse, allTabsUpdateEventRequestData, allTabsUpdateCaseData
        );

        if (PAID.equalsIgnoreCase(serviceRequestUpdateDto.getServiceRequestStatus())) {
            solicitorEmailService.sendEmail(allTabsUpdateStartEventResponse.getCaseDetails());
            caseWorkerEmailService.sendEmail(allTabsUpdateStartEventResponse.getCaseDetails());
        }
    }

    private void syncPaymentMetadata(ServiceRequestUpdateDto dto, String auth, String userId, CaseData caseData) {
        boolean isCasePayment = !StringUtils.isEmpty(dto.getServiceRequestReference())
            && dto.getServiceRequestReference().equalsIgnoreCase(caseData.getPaymentServiceRequestReferenceNumber());

        CaseEvent event = getCaseEvent(isCasePayment, dto.getServiceRequestStatus());
        log.info("Triggering event {} for case {}", event.getValue(), dto.getCcdCaseNumber());

        EventRequestData eventRequestData = coreCaseDataService.eventRequest(event, userId);
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(auth, eventRequestData, dto.getCcdCaseNumber(), true);

        if (isCasePayment) {
            // Standard Case Payment: Single pass is usually sufficient
            CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContent(
                startEventResponse,
                setCaseData(dto)
            );
            coreCaseDataService.submitUpdate(auth, eventRequestData, caseDataContent, dto.getCcdCaseNumber(), true);

            // Original code triggered an All Tabs update here to refresh the UI flags
            triggerAllTabsUpdate(dto, auth, userId);
        } else {
            // --- AwP FLOW: RESTORING THE ORIGINAL LOGIC ---

            // First, submit the initial update to ensure the case is in the right state
            CaseDataContent initialContent = coreCaseDataService.createCaseDataContent(startEventResponse, new HashMap<>());
            coreCaseDataService.submitUpdate(auth, eventRequestData, initialContent, dto.getCcdCaseNumber(), true);

            // SECOND PASS: Start a new event (Update All Tabs) to fetch the REFRESHED case data
            // This ensures the additionalApplicationsBundle is no longer null.
            EventRequestData refreshRequestData = coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, userId);
            StartEventResponse refreshedResponse = coreCaseDataService.startUpdate(auth, refreshRequestData, dto.getCcdCaseNumber(), true);

            // Now map the data using the fresh response that actually contains the bundle
            Map<String, Object> awpData = setAwPPaymentCaseData(refreshedResponse, dto);

            CaseDataContent finalAwpContent = coreCaseDataService.createCaseDataContent(refreshedResponse, awpData);
            coreCaseDataService.submitUpdate(auth, refreshRequestData, finalAwpContent, dto.getCcdCaseNumber(), true);

            log.info("AwP Payment metadata synced successfully for case {}", dto.getCcdCaseNumber());
        }
    }

    private CaseEvent getCaseEvent(boolean isCasePayment, String status) {
        boolean isPaid = PAID.equalsIgnoreCase(status);
        if (isCasePayment) {
            return isPaid ? CaseEvent.PAYMENT_SUCCESS_CALLBACK : CaseEvent.PAYMENT_FAILURE_CALLBACK;
        } else {
            return isPaid ? CaseEvent.AWP_PAYMENT_SUCCESS_CALLBACK : CaseEvent.AWP_PAYMENT_FAILURE_CALLBACK;
        }
    }

    private boolean isAlreadyProcessed(CaseData caseData, ServiceRequestUpdateDto dto) {
        return caseData.getPaymentCallbackServiceRequestUpdate() != null
            && dto.getServiceRequestReference().equalsIgnoreCase(caseData.getPaymentCallbackServiceRequestUpdate().getServiceRequestReference());
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
                                                     .payment(CcdPayment.builder()
                                                                  .paymentAmount(serviceRequestUpdateDto.getPayment().getPaymentAmount())
                                                                  .paymentReference(serviceRequestUpdateDto.getPayment().getPaymentReference())
                                                                  .paymentMethod(serviceRequestUpdateDto.getPayment().getPaymentMethod())
                                                                  .caseReference(serviceRequestUpdateDto.getPayment().getCaseReference())
                                                                  .accountNumber(serviceRequestUpdateDto.getPayment().getAccountNumber())
                                                                  .build()).build()).build();
    }

    private Map<String, Object> setAwPPaymentCaseData(StartEventResponse startEventResponse, ServiceRequestUpdateDto serviceRequestUpdateDto) {
        log.info("Available fields in StartEventResponse: {}", startEventResponse.getCaseDetails().getData().keySet());
        CaseData startEventResponseData = CaseUtils.getCaseData(startEventResponse.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        if (startEventResponseData.getAdditionalApplicationsBundle() != null) {
            Optional<Element<AdditionalApplicationsBundle>> bundleElement = startEventResponseData.getAdditionalApplicationsBundle()
                .stream()
                .filter(x -> null != x.getValue().getPayment()
                    && x.getValue().getPayment().getPaymentServiceRequestReferenceNumber().equalsIgnoreCase(
                    serviceRequestUpdateDto.getServiceRequestReference()))
                .findFirst();

            if (bundleElement.isPresent() && PAID.equalsIgnoreCase(serviceRequestUpdateDto.getServiceRequestStatus())) {
                AdditionalApplicationsBundle updatedBundle = bundleElement.get().getValue().toBuilder()
                    .payment(bundleElement.get().getValue().getPayment().toBuilder()
                                 .status(PaymentStatus.PAID.getDisplayedValue()).build())
                    .c2DocumentBundle(null != bundleElement.get().getValue().getC2DocumentBundle()
                                          ? bundleElement.get().getValue().getC2DocumentBundle().toBuilder().applicationStatus(
                        ApplicationStatus.SUBMITTED.getDisplayedValue()).build() : null)
                    .otherApplicationsBundle(null != bundleElement.get().getValue().getOtherApplicationsBundle()
                                                 ? bundleElement.get().getValue().getOtherApplicationsBundle()
                        .toBuilder().applicationStatus(ApplicationStatus.SUBMITTED.getDisplayedValue()).build() : null)
                    .build();

                int index = startEventResponseData.getAdditionalApplicationsBundle().indexOf(bundleElement.get());
                if (index != -1) {
                    startEventResponseData.getAdditionalApplicationsBundle().set(index, ElementUtils.element(bundleElement.get().getId(), updatedBundle));
                }
                caseDataUpdated.put(WA_ADDITIONAL_APPLICATION_COLLECTION_ID, bundleElement.get().getId());
                caseDataUpdated.put("additionalApplicationsBundle", startEventResponseData.getAdditionalApplicationsBundle());
                caseDataUpdated.put(AWP_WA_TASK_NAME, uploadAdditionalApplicationUtils.getAwPTaskNameWhenPaymentCompleted(updatedBundle));
                caseDataUpdated.put(AWP_WA_TASK_TO_BE_CREATED, YES);
            }
        }
        return caseDataUpdated;
    }

    public CaseData getCaseDataWithStateAndDateSubmitted(ServiceRequestUpdateDto serviceRequestUpdateDto, CaseData caseData) {
        try {
            Court closestCourt = courtFinderService.getNearestFamilyCourt(caseData);
            if (serviceRequestUpdateDto.getServiceRequestStatus().equalsIgnoreCase(PAID)) {
                ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
                caseData = caseData.toBuilder()
                    .state(shouldUpdateCaseState(caseData) ? State.SUBMITTED_PAID : caseData.getState())
                    .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
                    .build();
            } else {
                caseData = caseData.toBuilder()
                    .state(shouldUpdateCaseState(caseData) ? State.SUBMITTED_NOT_PAID : caseData.getState())
                    .build();
            }
            if (closestCourt != null) {
                caseData = caseData.toBuilder()
                    .courtName(closestCourt.getCourtName())
                    .courtId(String.valueOf(closestCourt.getCountyLocationCode()))
                    .courtCodeFromFact(String.valueOf(closestCourt.getCountyLocationCode()))
                    .build();
            }
        } catch (Exception e) {
            log.error("Error populating case date for case {}", caseData.getId(), e);
        }
        return caseData;
    }

    private void triggerAllTabsUpdate(ServiceRequestUpdateDto dto, String auth, String userId) {
        EventRequestData allTabsRequest = coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, userId);
        StartEventResponse allTabsResponse = coreCaseDataService.startUpdate(auth, allTabsRequest, dto.getCcdCaseNumber(), true);
        CaseDataContent content = coreCaseDataService.createCaseDataContent(allTabsResponse, new HashMap<>());
        coreCaseDataService.submitUpdate(auth, allTabsRequest, content, dto.getCcdCaseNumber(), true);
    }

    private boolean shouldUpdateCaseState(CaseData caseData) {
        return caseData.getState() == State.SUBMITTED_NOT_PAID || caseData.getState() == State.CASE_WITHDRAWN;
    }
}
