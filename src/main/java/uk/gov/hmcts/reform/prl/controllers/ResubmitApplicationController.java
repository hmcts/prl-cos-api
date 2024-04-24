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
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.prl.enums.caseworkeremailnotification.CaseWorkerEmailNotificationEventEnum;
import uk.gov.hmcts.reform.prl.enums.solicitoremailnotification.SolicitorEmailNotificationEventEnum;
import uk.gov.hmcts.reform.prl.events.CaseWorkerNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.events.SolicitorNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeFileUploadService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
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
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATE_AND_TIME_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_CODE_FROM_FACT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ID_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SecurityRequirement(name = "Bearer Authentication")
public class ResubmitApplicationController {
    private final CourtFinderService courtFinderService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final CaseEventService caseEventService;
    private final DocumentGenService documentGenService;
    private final OrganisationService organisationService;
    private final AllTabServiceImpl allTabService;
    private final ConfidentialityTabService confidentialityTabService;
    private final AuthorisationService authorisationService;
    private final EventService eventPublisher;

    private final MiamPolicyUpgradeService miamPolicyUpgradeService;

    private final MiamPolicyUpgradeFileUploadService miamPolicyUpgradeFileUploadService;

    private final SystemUserService systemUserService;

    @PostMapping(path = "/resubmit-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to change the state and document generation and submit application. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resubmission completed"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse resubmitApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseDetails caseDetails = callbackRequest.getCaseDetails();

            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            Map<String, Object> caseDataUpdated = new HashMap<>(caseDetails.getData());
            //Populate MIAM Policy Upgrade data
            if (C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))
                && TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())
                && isNotEmpty(caseData.getMiamPolicyUpgradeDetails())) {
                caseData = miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(caseData, caseDataUpdated);
                caseData = miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(
                    caseData,
                    systemUserService.getSysUserToken()
                );
                allTabService.getNewMiamPolicyUpgradeDocumentMap(caseData, caseDataUpdated);
            }

            //SNI-5695 fix-- if court name is already present then do not update
            if (StringUtils.isBlank(caseData.getCourtName())) {
                Court closestChildArrangementsCourt = courtFinderService
                    .getNearestFamilyCourt(caseData);
                caseData = assignCourtDetailsBasedOnClosestChildArrangementCourt(closestChildArrangementsCourt, caseData, caseDataUpdated);
            }

            List<CaseEventDetail> eventsForCase = caseEventService.findEventsForCase(String.valueOf(caseData.getId()));
            Optional<String> previousStates = eventsForCase.stream().map(CaseEventDetail::getStateId).filter(
                ResubmitApplicationController::getPreviousState).findFirst();

            updateCaseDataBasedOnState(authorisation, callbackRequest, caseData, caseDataUpdated, previousStates);

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated)
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private void updateCaseDataBasedOnState(String authorisation,
                                            CallbackRequest callbackRequest,
                                            CaseData caseData,
                                            Map<String, Object> caseDataUpdated,
                                            Optional<String> previousStates) throws Exception {
        if (previousStates.isPresent()) {
            if (State.SUBMITTED_PAID.getValue().equalsIgnoreCase(previousStates.get())) {
                caseData = caseData.toBuilder().state(State.SUBMITTED_PAID).build();
                caseDataUpdated.put(STATE_FIELD, State.SUBMITTED_PAID);
                ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
                caseData = caseData.setDateSubmittedDate();
                caseDataUpdated.put(DATE_SUBMITTED_FIELD, DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));
                caseDataUpdated.put(
                    CASE_DATE_AND_TIME_SUBMITTED_FIELD,
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime)
                );
                CaseWorkerNotificationEmailEvent caseWorkerNotificationEmailEvent = prepareCaseworkerEvent(
                    CaseWorkerEmailNotificationEventEnum.reSubmitEmailNotification,
                    callbackRequest
                );
                SolicitorNotificationEmailEvent solicitorNotificationEmailEvent = prepareSolicitorNotificationEvent(
                    CaseWorkerEmailNotificationEventEnum.reSubmitEmailNotification.getDisplayedValue(),
                    callbackRequest
                );

                eventPublisher.publishEvent(caseWorkerNotificationEmailEvent);
                eventPublisher.publishEvent(solicitorNotificationEmailEvent);
            }
            if (State.CASE_ISSUED.getValue().equalsIgnoreCase(previousStates.get())
                || State.JUDICIAL_REVIEW.getValue().equalsIgnoreCase(previousStates.get())) {
                caseData = organisationService.getApplicantOrganisationDetails(caseData);
                caseData = organisationService.getRespondentOrganisationDetails(caseData);
                caseData = caseData.setIssueDate();
                caseData = caseData.toBuilder().state(State.fromValue((previousStates.get()))).build();

                caseDataUpdated.put(STATE_FIELD, State.fromValue((previousStates.get())));
                caseDataUpdated.put(PrlAppsConstants.ISSUE_DATE_FIELD, caseData.getIssueDate());

                CaseWorkerNotificationEmailEvent courtAdminNotificationEmailEvent = prepareCaseworkerEvent(
                    CaseWorkerEmailNotificationEventEnum.sendEmailToCourtAdmin,
                    callbackRequest
                );
                eventPublisher.publishEvent(courtAdminNotificationEmailEvent);
            }
            // All docs will be regenerated in both issue and submitted state jira FPET-21
            caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));
            if (C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
                caseDataUpdated.putAll(documentGenService.generateDraftDocumentsForC100CaseResubmission(
                    authorisation,
                    caseData
                ));
            }
            caseDataUpdated.putAll(confidentialityTabService.updateConfidentialityDetails(caseData));
            caseDataUpdated.putAll(allTabService.getAllTabsFields(caseData));
            // remove the tick from submit screens so not present if resubmitted again
            caseDataUpdated.put(
                "confidentialityDisclaimerSubmit",
                Collections.singletonMap("confidentialityChecksChecked", null)
            );
            caseDataUpdated.put("submitAgreeStatement", null);
        }
    }

    private static CaseData assignCourtDetailsBasedOnClosestChildArrangementCourt(Court closestChildArrangementsCourt,
                                                                                  CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (closestChildArrangementsCourt != null && null != caseData.getCourtId()) {
            caseData = caseData.toBuilder()
                .courtName(closestChildArrangementsCourt.getCourtName())
                .courtId(String.valueOf(closestChildArrangementsCourt.getCountyLocationCode()))
                .build();
            caseDataUpdated.put(COURT_NAME_FIELD, closestChildArrangementsCourt.getCourtName());
            caseDataUpdated.put(
                COURT_ID_FIELD,
                String.valueOf(closestChildArrangementsCourt.getCountyLocationCode())
            );
            caseDataUpdated.put(
                COURT_CODE_FROM_FACT,
                String.valueOf(closestChildArrangementsCourt.getCountyLocationCode())
            );
        }
        return caseData;
    }

    private SolicitorNotificationEmailEvent prepareSolicitorNotificationEvent(String reSubmitEmailNotification, CallbackRequest callbackRequest) {
        return SolicitorNotificationEmailEvent.builder()
            .typeOfEvent(reSubmitEmailNotification)
            .caseDetailsModel(callbackRequest.getCaseDetails())
            .build();
    }

    @PostMapping(path = "/fl401/resubmit-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to change the state and send notifications.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resubmission completed"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse fl401resubmitApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {

            CaseDetails caseDetails = callbackRequest.getCaseDetails();
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

            List<CaseEventDetail> eventsForCase = caseEventService.findEventsForCase(String.valueOf(caseData.getId()));
            Optional<String> previousStates = eventsForCase.stream().map(CaseEventDetail::getStateId).filter(
                ResubmitApplicationController::getPreviousState).findFirst();
            Map<String, Object> caseDataUpdated = new HashMap<>(caseDetails.getData());

            if (previousStates.isPresent() && (State.SUBMITTED_PAID.getValue().equalsIgnoreCase(previousStates.get()))) {
                caseData = caseData.toBuilder().state(State.SUBMITTED_PAID).build();
                caseDataUpdated.put(STATE_FIELD, State.SUBMITTED_PAID);
                ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
                caseData = caseData.setDateSubmittedDate();
                caseDataUpdated.put(DATE_SUBMITTED_FIELD, caseData.getDateSubmitted());
                caseDataUpdated.put(
                    CASE_DATE_AND_TIME_SUBMITTED_FIELD,
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime)
                );
            }
            if (previousStates.isPresent() && (State.CASE_ISSUED.getValue().equalsIgnoreCase(previousStates.get())
                || (State.JUDICIAL_REVIEW.getValue().equalsIgnoreCase(previousStates.get())))) {
                caseData = caseData.toBuilder().state(State.fromValue(previousStates.get())).build();
                caseDataUpdated.put(STATE_FIELD, State.fromValue(previousStates.get()));
                caseData = caseData.setIssueDate();
                caseDataUpdated.put(ISSUE_DATE_FIELD, caseData.getIssueDate());
            }
            try {
                CaseWorkerNotificationEmailEvent sendEmailToFl401CourtEvent = prepareCaseworkerEvent(
                    CaseWorkerEmailNotificationEventEnum.notifyLocalCourt,
                    callbackRequest
                );
                SolicitorNotificationEmailEvent solicitorNotificationEmailEvent = prepareSolicitorNotificationEvent(
                    SolicitorEmailNotificationEventEnum.fl401SendEmailNotification.getDisplayedValue(),
                    callbackRequest
                );
                eventPublisher.publishEvent(solicitorNotificationEmailEvent);
                eventPublisher.publishEvent(sendEmailToFl401CourtEvent);
                caseDataUpdated.put("isNotificationSent", "Yes");
            } catch (Exception e) {
                log.error("Notification could not be sent due to ", e);
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
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private CaseWorkerNotificationEmailEvent prepareCaseworkerEvent(CaseWorkerEmailNotificationEventEnum notifyLocalCourt,
                                                                    CallbackRequest callbackRequest) {
        return CaseWorkerNotificationEmailEvent.builder()
            .typeOfEvent(notifyLocalCourt.getDisplayedValue())
            .caseDetailsModel(callbackRequest.getCaseDetails())
            .build();
    }

    private static boolean getPreviousState(String eachState) {
        return (!eachState.equalsIgnoreCase(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()))
            && (eachState.equalsIgnoreCase(State.SUBMITTED_PAID.getValue())
            || eachState.equalsIgnoreCase(State.CASE_ISSUED.getValue())
            || eachState.equalsIgnoreCase(State.JUDICIAL_REVIEW.getValue()));
    }
}
