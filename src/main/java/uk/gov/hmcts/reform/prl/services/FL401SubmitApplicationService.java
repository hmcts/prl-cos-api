package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.caseworkeremailnotification.CaseWorkerEmailNotificationEventEnum;
import uk.gov.hmcts.reform.prl.enums.solicitoremailnotification.SolicitorEmailNotificationEventEnum;
import uk.gov.hmcts.reform.prl.events.CaseWorkerNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.events.SolicitorNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATE_AND_TIME_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COLON_SEPERATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_EMAIL_ADDRESS_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_SEAL_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;

@Service
@Slf4j
@RequiredArgsConstructor
public class FL401SubmitApplicationService {
    private final UserService userService;
    private final AllTabServiceImpl allTabService;
    private final DocumentGenService documentGenService;
    private final LocationRefDataService locationRefDataService;
    private final ObjectMapper objectMapper;
    private final CourtSealFinderService courtSealFinderService;
    private final EventService eventPublisher;
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    public Map<String, Object> fl401GenerateDocumentSubmitApplication(String authorisation,
                                                                      CallbackRequest callbackRequest, CaseData caseData) throws Exception {
        caseData = caseData.toBuilder()
            .solicitorName(userService.getUserDetails(authorisation).getFullName())
            .build();

        final LocalDate localDate = LocalDate.now();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        String baseLocationId = caseData.getSubmitCountyCourtSelection().getValue().getCode().split(":")[0];
        Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId(
            baseLocationId,
            authorisation
        );

        Map<String, Object> courtDetailsMap = CaseUtils.getCourtDetails(courtVenue, baseLocationId);
        courtDetailsMap.put("submitCountyCourtSelection", DynamicList.builder()
            .value(caseData.getSubmitCountyCourtSelection().getValue()).build());
        caseData = caseData.toBuilder().issueDate(localDate).courtName(courtDetailsMap.containsKey(COURT_NAME_FIELD) ? courtDetailsMap.get(
            COURT_NAME_FIELD).toString() : null)
            .isCourtEmailFound(YES)
            .build();

        if (courtVenue.isPresent()) {
            String regionId = courtVenue.get().getRegionId();
            String courtSeal = courtSealFinderService.getCourtSeal(regionId);
            caseDataUpdated.put(COURT_SEAL_FIELD, courtSeal);
        }

        String courtEmail = caseData.getSubmitCountyCourtSelection().getValue().getCode().split(COLON_SEPERATOR).length > 1
            ? caseData.getSubmitCountyCourtSelection().getValue().getCode().split(COLON_SEPERATOR)[1] : null;
        caseDataUpdated.put(COURT_EMAIL_ADDRESS_FIELD, courtEmail);

        caseDataUpdated.putAll(courtDetailsMap);

        Optional<TypeOfApplicationOrders> typeOfApplicationOrders = ofNullable(caseData.getTypeOfApplicationOrders());
        if (typeOfApplicationOrders.isPresent()) {
            if (typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)) {
                caseData = caseData.toBuilder()
                    .respondentBehaviourData(null)
                    .build();
            } else if (typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder)) {
                caseData = caseData.toBuilder()
                    .home(null)
                    .build();
            }
        }

        caseData = caseData.setDateSubmittedDate();

        caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));

        caseDataUpdated.put(ISSUE_DATE_FIELD, localDate);

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

        caseDataUpdated.put(DATE_SUBMITTED_FIELD, DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));
        caseDataUpdated.put(
            CASE_DATE_AND_TIME_SUBMITTED_FIELD,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime)
        );


        caseDataUpdated.putAll(allTabService.getAllTabsFields(caseData));
        caseDataUpdated.put("caseFlags", Flags.builder().build());
        caseDataUpdated.putAll(partyLevelCaseFlagsService.generatePartyCaseFlags(caseData));
        return caseDataUpdated;
    }

    public CaseData fl401SendApplicationNotification(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        UserDetails userDetails = userService.getUserDetails(authorisation);
        try {
            SolicitorNotificationEmailEvent event = prepareFl401SolNotificationEvent(callbackRequest, userDetails);
            eventPublisher.publishEvent(event);
            if (null != caseData.getCourtEmailAddress()) {
                CaseWorkerNotificationEmailEvent caseWorkerNotificationEmailEvent = prepareCaseWorkerEmailEvent(callbackRequest, caseData);
                eventPublisher.publishEvent(caseWorkerNotificationEmailEvent);
            }
            caseData = caseData.toBuilder()
                .isNotificationSent("Yes")
                .build();

        } catch (Exception e) {
            log.error("Notification could not be sent due to ", e);
            caseData = caseData.toBuilder()
                .isNotificationSent("No")
                .build();
        }
        return caseData;
    }

    private CaseWorkerNotificationEmailEvent prepareCaseWorkerEmailEvent(CallbackRequest callbackRequest, CaseData caseData) {
        return CaseWorkerNotificationEmailEvent
            .builder()
            .typeOfEvent(CaseWorkerEmailNotificationEventEnum.notifyLocalCourt.getDisplayedValue())
            .caseDetailsModel(callbackRequest.getCaseDetails())
            .courtEmailAddress(caseData.getCourtEmailAddress())
            .build();
    }

    private SolicitorNotificationEmailEvent prepareFl401SolNotificationEvent(CallbackRequest callbackRequest, UserDetails userDetails) {
        return SolicitorNotificationEmailEvent.builder()
            .typeOfEvent(SolicitorEmailNotificationEventEnum.fl401SendEmailNotification.getDisplayedValue())
            .caseDetailsModel(callbackRequest.getCaseDetails())
            .userDetails(userDetails)
            .build();
    }
}
