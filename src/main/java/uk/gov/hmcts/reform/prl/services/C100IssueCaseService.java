package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.caseworkeremailnotification.CaseWorkerEmailNotificationEventEnum;
import uk.gov.hmcts.reform.prl.enums.solicitoremailnotification.SolicitorEmailNotificationEventEnum;
import uk.gov.hmcts.reform.prl.events.CaseWorkerNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.events.SolicitorNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COLON_SEPERATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_CODE_FROM_FACT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_SEAL_FIELD;

@Service
@Slf4j
@RequiredArgsConstructor
public class C100IssueCaseService {

    private final AllTabServiceImpl allTabsService;
    private final DocumentGenService documentGenService;
    private final CaseWorkerEmailService caseWorkerEmailService;
    private final LocationRefDataService locationRefDataService;
    private final CourtSealFinderService courtSealFinderService;
    private final CourtFinderService courtFinderService;
    private final ObjectMapper objectMapper;
    private final EventService eventPublisher;

    public Map<String, Object> issueAndSendToLocalCourt(String authorisation, CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        if (null != caseData.getCourtList() && null != caseData.getCourtList().getValue()) {
            String baseLocationId = caseData.getCourtList().getValue().getCode().split(COLON_SEPERATOR)[0];
            Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId(
                baseLocationId,
                authorisation
            );
            caseDataUpdated.putAll(CaseUtils.getCourtDetails(courtVenue, baseLocationId));
            caseDataUpdated.put("courtList", DynamicList.builder().value(caseData.getCourtList().getValue()).build());
            if (courtVenue.isPresent()) {
                String courtId = getFactCourtId(courtVenue.get());
                String courtSeal = courtSealFinderService.getCourtSeal(courtVenue.get().getRegionId());
                caseData = caseData.toBuilder().courtName(courtVenue.get().getCourtName())
                    .courtSeal(courtSeal).courtId(baseLocationId)
                    .courtCodeFromFact(courtId).build();
                caseDataUpdated.put(COURT_SEAL_FIELD, courtSeal);
                caseDataUpdated.put(COURT_CODE_FROM_FACT, courtId);
            }
            caseDataUpdated.put("localCourtAdmin", List.of(Element.<LocalCourtAdminEmail>builder().id(UUID.randomUUID())
                                                               .value(LocalCourtAdminEmail
                                                                          .builder()
                                                                          .email(caseData.getCourtList().getValue().getCode().split(
                                                                              COLON_SEPERATOR).length > 1
                                                                                     ? caseData.getCourtList().getValue().getCode().split(
                                                                              COLON_SEPERATOR)[1] : null)
                                                                          .build())
                                                               .build()));

            caseData = caseData.toBuilder().issueDate(LocalDate.now())
                .courtName(caseDataUpdated.containsKey(COURT_NAME_FIELD) ? caseDataUpdated.get(COURT_NAME_FIELD).toString() : null)
                .build();
        }

        // Generate All Docs and set to casedataupdated.
        caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));

        // Refreshing the page in the same event. Hence no external event call needed.
        // Getting the tab fields and add it to the casedetails..
        Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);
        caseDataUpdated.putAll(allTabsFields);
        caseDataUpdated.put("issueDate", caseData.getIssueDate());
        return caseDataUpdated;
    }

    public String getFactCourtId(CourtVenue courtVenue) {
        String courtId = "";
        String factUrl = courtVenue.getFactUrl();
        if (factUrl != null && factUrl.split("/").length > 4) {
            Court court = null;
            try {
                court = courtFinderService.getCourtDetails(factUrl.split("/")[4]);
            } catch (Exception ex) {
                log.error("Error fetching court details from Fact ", ex);
            }
            if (court != null) {
                courtId = String.valueOf(court.getCountyLocationCode());
            }
        }
        return courtId;
    }

    public void issueAndSendToLocalCourNotification(CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        if (YesOrNo.No.equals(caseData.getConsentOrder())) {
            SolicitorNotificationEmailEvent rpaEmailNotificationEvent = SolicitorNotificationEmailEvent.builder()
                .typeOfEvent(SolicitorEmailNotificationEventEnum.notifyRpa.getDisplayedValue())
                .caseDetailsModel(callbackRequest.getCaseDetails())
                .build();
            eventPublisher.publishEvent(rpaEmailNotificationEvent);
        }
        CaseWorkerNotificationEmailEvent notifyLocalCourtEvent = CaseWorkerNotificationEmailEvent.builder()
            .typeOfEvent(CaseWorkerEmailNotificationEventEnum.sendEmailToCourtAdmin.getDisplayedValue())
            .caseDetailsModel(callbackRequest.getCaseDetails())
            .build();
        eventPublisher.publishEvent(notifyLocalCourtEvent);
    }
}
