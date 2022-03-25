package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_AND_TIME_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C100ReSubmitApplicationController {

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

    @PostMapping(path = "/resubmit-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to change the state and document generation and submit application. ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Resubmission completed"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse resubmitApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        List<CaseEventDetail> eventsForCase = caseEventService.findEventsForCase(String.valueOf(caseData.getId()));
        Optional<String> previousStates = eventsForCase.stream().map(CaseEventDetail::getStateId).filter(
            C100ReSubmitApplicationController::getPreviousState).findFirst();
        Map<String, Object> caseDataUpdated = new HashMap<>(caseDetails.getData());
        if (previousStates.isPresent()) {
            // For submitted state - No docs will be generated.
            if (State.SUBMITTED_PAID.getValue().equalsIgnoreCase(previousStates.get())) {
                caseData = caseData.toBuilder().state(State.SUBMITTED_PAID).build();
                caseDataUpdated.put(STATE_FIELD, State.SUBMITTED_PAID);
                ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
                caseData = caseData.setDateSubmittedDate();
                caseDataUpdated.put(DATE_SUBMITTED_FIELD, caseData.getDateSubmitted());
                caseDataUpdated.put(DATE_AND_TIME_SUBMITTED_FIELD, DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));
                caseWorkerEmailService.sendEmail(caseDetails);
                solicitorEmailService.sendEmail(caseDetails);
            }
            // For Case issue state - All docs will be regenerated.
            if (State.CASE_ISSUE.getValue().equalsIgnoreCase(previousStates.get())) {
                caseData = organisationService.getApplicantOrganisationDetails(caseData);
                caseData = organisationService.getRespondentOrganisationDetails(caseData);
                caseData = caseData.setIssueDate();
                caseData = caseData.toBuilder().state(State.CASE_ISSUE).build();

                caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));
                caseDataUpdated.put(STATE_FIELD, State.CASE_ISSUE);
                caseDataUpdated.put(PrlAppsConstants.ISSUE_DATE_FIELD, caseData.getIssueDate());
                caseWorkerEmailService.sendEmailToCourtAdmin(callbackRequest.getCaseDetails());
            }

            caseDataUpdated.putAll(allTabService.getAllTabsFields(caseData));
            caseDataUpdated.put("confidentialityDisclaimerSubmit", Collections.singletonMap("confidentialityChecksChecked", null));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }

    private static boolean getPreviousState(String eachState) {
        return (!eachState.equalsIgnoreCase(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()))
            && (eachState.equalsIgnoreCase(State.SUBMITTED_PAID.getValue())
            || eachState.equalsIgnoreCase(State.CASE_ISSUE.getValue()));
    }
}
