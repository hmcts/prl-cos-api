package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.Correspondence;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.ExampleService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.workflows.ApplicationConsiderationTimetableValidationWorkflow;
import uk.gov.hmcts.reform.prl.workflows.ValidateMiamApplicationOrExemptionWorkflow;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.GATEKEEPING_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PENDING_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RETURN_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBMITTED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WITHDRAWN_STATE;
import static uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts.restrictToGroup;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CallbackController {
    private final CaseEventService caseEventService;
    private final ApplicationConsiderationTimetableValidationWorkflow applicationConsiderationTimetableValidationWorkflow;
    private final ExampleService exampleService;
    private final OrganisationService organisationService;
    private final ValidateMiamApplicationOrExemptionWorkflow validateMiamApplicationOrExemptionWorkflow;
    private final SolicitorEmailService solicitorEmailService;
    private final CaseWorkerEmailService caseWorkerEmailService;

    private final ObjectMapper objectMapper;
    private final AllTabServiceImpl allTabsService;
    private final UserService userService;
    private final DocumentGenService documentGenService;

    private final SendgridService sendgridService;
    private final C100JsonMapper c100JsonMapper;

    @PostMapping(path = "/validate-application-consideration-timetable", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to validate application consideration timetable. Returns error messages if validation fails.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<uk.gov.hmcts.reform.ccd.client.model.CallbackResponse> validateApplicationConsiderationTimetable(
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws WorkflowException {
        WorkflowResult workflowResult = applicationConsiderationTimetableValidationWorkflow.run(callbackRequest);

        return ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .errors(workflowResult.getErrors())
                .build()
        );
    }

    @PostMapping(path = "/validate-miam-application-or-exemption", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to confirm that a MIAM has been attended or applicant is exempt. Returns error message if confirmation fails")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<uk.gov.hmcts.reform.ccd.client.model.CallbackResponse> validateMiamApplicationOrExemption(
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws WorkflowException {
        WorkflowResult workflowResult = validateMiamApplicationOrExemptionWorkflow.run(callbackRequest);

        return ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .errors(workflowResult.getErrors())
                .build()

        );
    }

    @PostMapping(path = "/generate-save-draft-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to generate and store document")
    public AboutToStartOrSubmitCallbackResponse generateAndStoreDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody @ApiParam("CaseData") uk.gov.hmcts.reform.ccd.client.model.CallbackRequest request
    ) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(request.getCaseDetails(), objectMapper);

        if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseData = buildTypeOfApplicationCaseData(caseData);
        }

        Map<String, Object> caseDataUpdated = request.getCaseDetails().getData();

        // Generate draft documents and set to casedataupdated..
        caseDataUpdated.putAll(documentGenService.generateDraftDocuments(authorisation, caseData));

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    private CaseData buildTypeOfApplicationCaseData(CaseData caseData) {
        Optional<TypeOfApplicationOrders> typeOfApplicationOrders = ofNullable(caseData.getTypeOfApplicationOrders());
        if (typeOfApplicationOrders.isEmpty() || (typeOfApplicationOrders.get().getOrderType().contains(
            FL401OrderTypeEnum.occupationOrder)
            && typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder))) {
            caseData = caseData.toBuilder().build();
        } else if (typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)) {
            caseData = caseData.toBuilder()
                .respondentBehaviourData(null)
                .build();
        } else if (typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder)) {
            caseData = caseData.toBuilder()
                .home(null)
                .build();
        }
        return caseData;
    }

    @PostMapping(path = "/issue-and-send-to-local-court", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to Issue and send to local court")
    public AboutToStartOrSubmitCallbackResponse issueAndSendToLocalCourt(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) throws Exception {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        if (YesOrNo.No.equals(caseData.getConsentOrder())) {
            requireNonNull(caseData);
            sendgridService.sendEmail(c100JsonMapper.map(caseData));
        }
        caseData = caseData.toBuilder().issueDate(LocalDate.now()).build();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        // Generate All Docs and set to casedataupdated.
        caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));

        // Refreshing the page in the same event. Hence no external event call needed.
        // Getting the tab fields and add it to the casedetails..
        Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);

        caseDataUpdated.putAll(allTabsFields);
        caseDataUpdated.put("issueDate", caseData.getIssueDate());

        try {
            caseWorkerEmailService.sendEmailToCourtAdmin(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            log.info("Email notification could not be sent due to following {}", ex.getMessage());
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


    @PostMapping(path = "/update-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to refresh the tabs")
    public void updateApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        allTabsService.updateAllTabs(caseData);
    }

    @PostMapping(path = "/case-withdrawn-email-notification", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Send Email Notification on Case Withdraw")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse sendEmailNotificationOnCaseWithdraw(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<CaseEventDetail> eventsForCase = caseEventService.findEventsForCase(String.valueOf(caseData.getId()));

        Optional<String> previousState = eventsForCase.stream().map(CaseEventDetail::getStateId)
            .filter(
                CallbackController::getPreviousState).findFirst();

        UserDetails userDetails = userService.getUserDetails(authorisation);
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        List<String> stateList = List.of(DRAFT_STATE, "CLOSED",
                                         PENDING_STATE,
                                         SUBMITTED_STATE, RETURN_STATE
        );

        boolean previousStateInList = previousState.filter(stateList::contains).isPresent();

        WithdrawApplication withDrawApplicationData = caseData.getWithDrawApplicationData();
        Optional<YesOrNo> withdrawApplication = ofNullable(withDrawApplicationData.getWithDrawApplication());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if ((withdrawApplication.isPresent() && Yes.equals(withdrawApplication.get()))) {
            if (previousState.isPresent() && !stateList.contains(previousState.get())) {
                caseDataUpdated.put("isWithdrawRequestSent", "Pending");
                log.info("**** Case is updated as WithdrawRequestSent **** ");
                if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                    solicitorEmailService.sendWithDrawEmailToSolicitorAfterIssuedState(caseDetails, userDetails);
                    Optional<List<Element<LocalCourtAdminEmail>>> localCourtAdmin = ofNullable(caseData.getLocalCourtAdmin());
                    if (localCourtAdmin.isPresent()) {
                        Optional<LocalCourtAdminEmail> localCourtAdminEmail = localCourtAdmin.get().stream().map(Element::getValue)
                            .findFirst();
                        if (localCourtAdminEmail.isPresent()) {
                            String email = localCourtAdminEmail.get().getEmail();
                            caseWorkerEmailService.sendWithdrawApplicationEmailToLocalCourt(caseDetails, email);
                        }
                    }
                } else {
                    solicitorEmailService.sendWithDrawEmailToFl401SolicitorAfterIssuedState(caseDetails, userDetails);
                    caseWorkerEmailService.sendWithdrawApplicationEmailToLocalCourt(caseDetails,
                                                                                    caseData.getCourtEmailAddress());
                }
            } else {
                if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                    solicitorEmailService.sendWithDrawEmailToSolicitor(caseDetails, userDetails);
                    // Refreshing the page in the same event. Hence no external event call needed.
                    // Getting the tab fields and add it to the casedetails..
                    Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);
                    caseDataUpdated.putAll(allTabsFields);
                } else if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                    solicitorEmailService.sendWithDrawEmailToFl401Solicitor(caseDetails, userDetails);
                }
                caseDataUpdated.put("state", WITHDRAWN_STATE);
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    public void sendC100CaseWithDrawEmails(CaseData caseData, CaseDetails caseDetails, UserDetails userDetails) {
        Optional<List<Element<LocalCourtAdminEmail>>> localCourtAdmin = ofNullable(caseData.getLocalCourtAdmin());
        if (localCourtAdmin.isPresent()) {
            String email = localCourtAdmin.get().get(0).getValue().getEmail();
            caseWorkerEmailService.sendWithdrawApplicationEmailToLocalCourt(caseDetails, email);
        }
        solicitorEmailService.sendWithDrawEmailToSolicitorAfterIssuedState(caseDetails, userDetails);
    }


    @PostMapping(path = "/send-to-gatekeeper", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Send Email Notification on Send to gatekeeper ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse sendEmailForSendToGatekeeper(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseWorkerEmailService.sendEmailToGateKeeper(caseDetails);

        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        Map<String, Object> caseDataUpdated = caseDetails.getData();

        Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);
        caseDataUpdated.putAll(allTabsFields);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/resend-rpa", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Resend case data json to RPA")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse resendNotificationtoRpa(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws IOException {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        requireNonNull(caseData);
        sendgridService.sendEmail(c100JsonMapper.map(caseData));
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/about-to-submit-case-creation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Copy fl401 case name to C100 Case name")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse aboutToSubmitCaseCreation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        // Updating the case name for FL401
        if (caseDataUpdated.get("applicantOrRespondentCaseName") != null) {
            caseDataUpdated.put("applicantCaseName", caseDataUpdated.get("applicantOrRespondentCaseName"));
        }

        // Saving the logged-in Solicitor and Org details for the docs..
        caseDataUpdated = getSolicitorDetails(authorisation, caseDataUpdated);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/fl401-add-case-number", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback for add case number submit event")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse addCaseNumberSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("issueDate", LocalDate.now());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    private static boolean getPreviousState(String eachState) {
        return (!WITHDRAWN_STATE.equalsIgnoreCase(eachState)
            && (!DRAFT_STATE.equalsIgnoreCase(eachState))
            && (!RETURN_STATE.equalsIgnoreCase(eachState))
            && (!PENDING_STATE.equalsIgnoreCase(eachState))
            && (!SUBMITTED_STATE.equalsIgnoreCase(eachState)))
            || ISSUED_STATE.equalsIgnoreCase(eachState)
            || GATEKEEPING_STATE.equalsIgnoreCase(eachState);
    }

    @PostMapping(path = "/copy-manage-docs-for-tabs", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Copy fl401 case name to C100 Case name")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse copyManageDocsForTabs(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<Element<FurtherEvidence>> furtherEvidences = caseData.getFurtherEvidences();
        List<Element<Correspondence>> correspondence = caseData.getCorrespondence();
        List<Element<OtherDocuments>> otherDocuments = caseData.getOtherDocuments();
        if (furtherEvidences != null) {
            furtherEvidences = furtherEvidences.stream()
                    .filter(element -> {
                        return element.getValue().getRestrictCheckboxFurtherEvidence().contains(restrictToGroup);
                    })
                .collect(Collectors.toList());
            caseDataUpdated.put("mainAppDocForTabDisplay", furtherEvidences);
        }
        if (correspondence != null) {
            correspondence = correspondence.stream()
                .filter(element -> {
                    return element.getValue().getRestrictCheckboxCorrespondence().contains(restrictToGroup);
                })
                .collect(Collectors.toList());
            caseDataUpdated.put("correspondenceForTabDisplay", correspondence);
        }
        if (otherDocuments != null) {

            otherDocuments = otherDocuments.stream()
                .filter(element -> {
                    return element.getValue().getRestrictCheckboxOtherDocuments().contains(restrictToGroup);
                })
                .collect(Collectors.toList());
            caseDataUpdated.put("otherDocumentsForTabDisplay", otherDocuments);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    private Map<String, Object> getSolicitorDetails(String authorisation, Map<String, Object> caseDataUpdated) {
        log.info("Fetching the user and Org Details ");
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            Optional<Organisations> userOrganisation = organisationService.findUserOrganisation(authorisation);
            caseDataUpdated.put("caseSolicitorName", userDetails.getFullName());
            if (userOrganisation.isPresent()) {
                log.info("Got the Org Details");
                caseDataUpdated.put("caseSolicitorOrgName", userOrganisation.get().getName());
            }
            log.info("SUCCESSFULLY fetched user and Org Details ");
        } catch (Exception e) {
            log.error("Error while fetching User or Org details for the logged in user ", e);
        }

        return caseDataUpdated;

    }
}


