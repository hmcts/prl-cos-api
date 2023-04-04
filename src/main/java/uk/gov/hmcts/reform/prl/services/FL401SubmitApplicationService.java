package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_EMAIL_ADDRESS_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ID_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_SEAL_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;

@Service
@Slf4j
@RequiredArgsConstructor
public class FL401SubmitApplicationService {

    @Autowired
    private CourtFinderService courtFinderService;

    @Autowired
    private UserService userService;

    @Autowired
    private AllTabServiceImpl allTabService;
    @Autowired
    private DocumentGenService documentGenService;

    @Autowired
    LocationRefDataService locationRefDataService;

    @Autowired
    private CourtFinderApi courtFinderApi;

    @Autowired
    private ConfidentialityTabService confidentialityTabService;

    @Autowired
    private SolicitorEmailService solicitorEmailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourtSealFinderService courtSealFinderService;

    public Map<String, Object> fl401GenerateDocumentSubmitApplication(String authorisation,
                                                                      CallbackRequest callbackRequest, CaseData caseData) throws Exception {
        caseData = caseData.toBuilder()
            .solicitorName(userService.getUserDetails(authorisation).getFullName())
            .build();

        final LocalDate localDate = LocalDate.now();

        log.info("****inside fl401GenerateDocumentSubmitApplication caseData:{}", caseData);
        String baseLocationId = caseData.getSubmitCountyCourtSelection().getValue().getCode();
        log.info("****inside fl401GenerateDocumentSubmitApplication baseLocationId:{}", baseLocationId);
        Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId(baseLocationId,
                                                                                             authorisation);
        Map<String, Object> courtDetailsMap = CaseUtils.getCourtDetails(courtVenue, baseLocationId);
        caseData = caseData.toBuilder().issueDate(localDate).courtName(courtDetailsMap.containsKey(COURT_NAME_FIELD) ? courtDetailsMap.get(
            COURT_NAME_FIELD).toString() : null).build();
        caseData = caseData.toBuilder().isCourtEmailFound("Yes").build();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.putAll(courtDetailsMap);
        if (courtVenue.isPresent()) {
            String courtSeal = courtSealFinderService.getCourtSeal(courtVenue.get().getRegionId());
            caseData = caseData.toBuilder().courtName(courtVenue.get().getCourtName())
                .courtSeal(courtSeal).build();
            caseDataUpdated.put(COURT_SEAL_FIELD, courtSeal);
        }
        if (courtVenue.isPresent()) {
            String postcode = courtVenue.get().getPostcode();
            String courtEmail = null;
            if (null != courtFinderApi.findClosestDomesticAbuseCourtByPostCode(postcode)
                && null != courtFinderApi.findClosestDomesticAbuseCourtByPostCode(postcode).getCourts()) {
                String courtSlug = courtFinderApi.findClosestDomesticAbuseCourtByPostCode(postcode).getCourts().get(0).getCourtSlug();
                Court court = courtFinderApi.getCourtDetails(courtSlug);
                caseDataUpdated.put(COURT_ID_FIELD, baseLocationId);
                Optional<CourtEmailAddress> optionalCourtEmail = courtFinderService.getEmailAddress(court);
                if (optionalCourtEmail.isPresent()) {
                    courtEmail = optionalCourtEmail.get().getAddress();
                }
            }
            caseDataUpdated.put(COURT_EMAIL_ADDRESS_FIELD, courtEmail);
        }

        Optional<TypeOfApplicationOrders> typeOfApplicationOrders = ofNullable(caseData.getTypeOfApplicationOrders());
        if (typeOfApplicationOrders.isEmpty() || (typeOfApplicationOrders.get().getOrderType().contains(
            FL401OrderTypeEnum.occupationOrder)
            && typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder))) {
            caseData = caseData.toBuilder().build();
        } else  if (typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)) {
            caseData = caseData.toBuilder()
                .respondentBehaviourData(null)
                .build();
        } else if (typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder)) {
            caseData = caseData.toBuilder()
                .home(null)
                .build();
        }
        caseData = caseData.setDateSubmittedDate();

        caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));

        caseDataUpdated.put(ISSUE_DATE_FIELD, localDate);

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

        caseDataUpdated.put(DATE_SUBMITTED_FIELD, DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));
        caseDataUpdated.put(CASE_DATE_AND_TIME_SUBMITTED_FIELD, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime));


        caseDataUpdated.putAll(allTabService.getAllTabsFields(caseData));
        return caseDataUpdated;
    }

    public CaseData fl401SendApplicationNotification(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        UserDetails userDetails = userService.getUserDetails(authorisation);

        try {
            solicitorEmailService.sendEmailToFl401Solicitor(callbackRequest.getCaseDetails(), userDetails);
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
