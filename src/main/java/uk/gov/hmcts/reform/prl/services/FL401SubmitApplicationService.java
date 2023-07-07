package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
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
    private final SolicitorEmailService solicitorEmailService;
    private final ObjectMapper objectMapper;
    private final CourtSealFinderService courtSealFinderService;
    private final CaseWorkerEmailService caseWorkerEmailService;

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
        return caseDataUpdated;
    }

    public CaseData fl401SendApplicationNotification(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        UserDetails userDetails = userService.getUserDetails(authorisation);

        try {
            solicitorEmailService.sendEmailToFl401Solicitor(callbackRequest.getCaseDetails(), userDetails);
            if (null != caseData.getCourtEmailAddress()) {
                caseWorkerEmailService.sendEmailToFl401LocalCourt(
                    callbackRequest.getCaseDetails(),
                    caseData.getCourtEmailAddress()
                );
            }
            caseData = caseData.toBuilder()
                .isNotificationSent("Yes")
                .build();

        } catch (Exception e) {
            log.error("Notification could not be sent due to {} ", e.getMessage());
            caseData = caseData.toBuilder()
                .isNotificationSent("No")
                .build();
        }
        return caseData;
    }
}
