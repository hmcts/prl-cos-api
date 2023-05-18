package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATE_AND_TIME_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ID_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SecurityRequirement(name = "Bearer Authentication")
public class ResubmitApplicationController {

    @Autowired
    private CourtFinderService courtFinderService;

    @Autowired
    private UserService userService;

    @Autowired
    private SolicitorEmailService solicitorEmailService;

    @Autowired
    private CaseWorkerEmailService caseWorkerEmailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseEventService caseEventService;

    @Autowired
    private DocumentGenService documentGenService;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    AllTabServiceImpl allTabService;

    @Autowired
    private ConfidentialityTabService confidentialityTabService;

    @PostMapping(path = "/resubmit-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to change the state and document generation and submit application. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resubmission completed"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse resubmitApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        Court closestChildArrangementsCourt = courtFinderService
            .getNearestFamilyCourt(caseData);
        if (closestChildArrangementsCourt != null) {
            caseData = caseData.toBuilder()
                .courtName(closestChildArrangementsCourt.getCourtName())
                .courtId(String.valueOf(closestChildArrangementsCourt.getCountyLocationCode()))
                .build();
        }

        Map<String, Object> caseDataUpdated = new HashMap<>(caseDetails.getData());
        if (closestChildArrangementsCourt != null) {
            caseDataUpdated.put(COURT_NAME_FIELD, closestChildArrangementsCourt.getCourtName());
            caseDataUpdated.put(COURT_ID_FIELD, String.valueOf(closestChildArrangementsCourt.getCountyLocationCode()));
        }

        List<CaseEventDetail> eventsForCase = caseEventService.findEventsForCase(String.valueOf(caseData.getId()));
        Optional<String> previousStates = eventsForCase.stream().map(CaseEventDetail::getStateId).filter(
            ResubmitApplicationController::getPreviousState).findFirst();

        if (previousStates.isPresent()) {
            if (State.SUBMITTED_PAID.getValue().equalsIgnoreCase(previousStates.get())) {
                caseData = caseData.toBuilder().state(State.SUBMITTED_PAID).build();
                caseDataUpdated.put(STATE_FIELD, State.SUBMITTED_PAID);
                ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
                caseData = caseData.setDateSubmittedDate();
                caseDataUpdated.put(DATE_SUBMITTED_FIELD, DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));
                caseDataUpdated.put(CASE_DATE_AND_TIME_SUBMITTED_FIELD, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime));
                caseWorkerEmailService.sendEmail(caseDetails);
                solicitorEmailService.sendReSubmitEmail(caseDetails);
            }
            if (State.CASE_ISSUED.getValue().equalsIgnoreCase(previousStates.get())
                || State.JUDICIAL_REVIEW.getValue().equalsIgnoreCase(previousStates.get())) {
                caseData = organisationService.getApplicantOrganisationDetails(caseData);
                caseData = organisationService.getRespondentOrganisationDetails(caseData);
                caseData = caseData.setIssueDate();
                caseData = caseData.toBuilder().state(State.fromValue((previousStates.get()))).build();

                caseDataUpdated.put(STATE_FIELD, State.fromValue((previousStates.get())));
                caseDataUpdated.put(PrlAppsConstants.ISSUE_DATE_FIELD, caseData.getIssueDate());
                caseWorkerEmailService.sendEmailToCourtAdmin(callbackRequest.getCaseDetails());
            }
            // All docs will be regenerated in both issue and submitted state jira FPET-21
            caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));
            caseDataUpdated.putAll(confidentialityTabService.updateConfidentialityDetails(caseData));
            caseDataUpdated.putAll(allTabService.getAllTabsFields(caseData));
            // remove the tick from submit screens so not present if resubmitted again
            caseDataUpdated.put("confidentialityDisclaimerSubmit", Collections.singletonMap("confidentialityChecksChecked", null));
            caseDataUpdated.put("submitAgreeStatement", null);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }

    @PostMapping(path = "/fl401/resubmit-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to change the state and send notifications.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resubmission completed"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse fl401resubmitApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        List<CaseEventDetail> eventsForCase = caseEventService.findEventsForCase(String.valueOf(caseData.getId()));
        Optional<String> previousStates = eventsForCase.stream().map(CaseEventDetail::getStateId).filter(
            ResubmitApplicationController::getPreviousState).findFirst();
        Map<String, Object> caseDataUpdated = new HashMap<>(caseDetails.getData());

        UserDetails userDetails = userService.getUserDetails(authorisation);

        if (previousStates.isPresent() && (State.SUBMITTED_PAID.getValue().equalsIgnoreCase(previousStates.get())
            || (State.JUDICIAL_REVIEW.getValue().equalsIgnoreCase(previousStates.get())))) {
            caseData = caseData.toBuilder().state(State.fromValue((previousStates.get()))).build();
            caseDataUpdated.put(STATE_FIELD, State.fromValue((previousStates.get())));
            ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
            caseData = caseData.setDateSubmittedDate();
            caseDataUpdated.put(DATE_SUBMITTED_FIELD, caseData.getDateSubmitted());
            caseDataUpdated.put(
                CASE_DATE_AND_TIME_SUBMITTED_FIELD,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime)
            );
        }
        if (previousStates.isPresent() && State.CASE_ISSUED.getValue().equalsIgnoreCase(previousStates.get())) {
            caseData = caseData.toBuilder().state(State.CASE_ISSUED).build();
            caseDataUpdated.put(STATE_FIELD, State.CASE_ISSUED);
            caseData = caseData.setIssueDate();
            caseDataUpdated.put(ISSUE_DATE_FIELD, caseData.getIssueDate());
        }
        try {
            solicitorEmailService.sendEmailToFl401Solicitor(caseDetails, userDetails);
            caseWorkerEmailService.sendEmailToFl401LocalCourt(caseDetails, caseData.getCourtEmailAddress());
            caseDataUpdated.put("isNotificationSent", "Yes");
        } catch (Exception e) {
            log.error("Notification could not be sent due to {} ", e.getMessage());
            caseDataUpdated.put("isNotificationSent", "No");
        }

        //set the resubmit fields to null so they are blank if multiple resubmissions
        caseDataUpdated.put("fl401StmtOfTruthResubmit", null);
        caseDataUpdated.put("fl401ConfidentialityCheckResubmit", null);
        caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));
        caseDataUpdated.putAll(allTabService.getAllTabsFields(caseData));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }

    private static boolean getPreviousState(String eachState) {
        return (!eachState.equalsIgnoreCase(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()))
            && (eachState.equalsIgnoreCase(State.SUBMITTED_PAID.getValue())
            || eachState.equalsIgnoreCase(State.CASE_ISSUED.getValue())
            || eachState.equalsIgnoreCase(State.JUDICIAL_REVIEW.getValue()));
    }
}
