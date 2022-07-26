package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.ExampleService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SearchCasesDataService;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_AND_TIME_SUBMITTED_FIELD;
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

    @Value("${document.templates.common.prl_draft_an_order_template}")
    protected String draftAnOrder;

    @Value("${document.templates.common.prl_draft_an_order_filename}")
    protected String draftAnOrderFile;

    @Autowired
    private final DgsService dgsService;
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

    private final CourtFinderService courtLocatorService;
    private final SearchCasesDataService searchCasesDataService;

    private final ConfidentialityTabService confidentialityTabService;

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

    @PostMapping(path = "/pre-populate-court-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to pre  populate court details")
    public AboutToStartOrSubmitCallbackResponse prePopulateCourtDetails(
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) throws NotFoundException {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        Court closestChildArrangementsCourt = courtLocatorService
            .getNearestFamilyCourt(caseData);
        Optional<CourtEmailAddress> courtEmailAddress = closestChildArrangementsCourt == null ? Optional.empty() : courtLocatorService
            .getEmailAddress(closestChildArrangementsCourt);
        if (courtEmailAddress.isPresent()) {
            log.info("Found court email for case id {}",caseData.getId());
            caseDataUpdated.put("localCourtAdmin",List.of(
                Element.<LocalCourtAdminEmail>builder().value(LocalCourtAdminEmail.builder().email(courtEmailAddress.get().getAddress()).build())
                    .build()));
        } else {
            log.info("Court email not found for case id {}",caseData.getId());
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/generate-document-submit-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to Generate document after submit application")
    public AboutToStartOrSubmitCallbackResponse generateDocumentSubmitApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) throws Exception {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        caseData = caseData.toBuilder().applicantsConfidentialDetails(confidentialityTabService
                .getConfidentialApplicantDetails(caseData.getApplicants().stream()
                .map(Element::getValue)
                .collect(Collectors.toList())))
            .childrenConfidentialDetails(confidentialityTabService.getChildrenConfidentialDetails(caseData.getChildren()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList()))).build();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        caseDataUpdated.put(
            DATE_AND_TIME_SUBMITTED_FIELD,
            DateTimeFormatter.ofPattern("d MMM yyyy, hh:mm:ssa", Locale.UK).format(zonedDateTime).toUpperCase());

        Map<String,Object> map = documentGenService.generateDocuments(authorisation, caseData);

        caseDataUpdated.putAll(map);

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
        WithdrawApplication withDrawApplicationData = caseData.getWithDrawApplicationData();
        Optional<YesOrNo> withdrawApplication = ofNullable(withDrawApplicationData.getWithDrawApplication());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if ((withdrawApplication.isPresent() && Yes.equals(withdrawApplication.get()))) {
            if (previousState.isPresent() && !stateList.contains(previousState.get())) {
                caseDataUpdated.put("isWithdrawRequestSent", "Pending");
                log.info("Case is updated as WithdrawRequestSent");
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

    @PostMapping(path = "/update-applicant-child-names", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Resend case data json to RPA")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse updateApplicantAndChildNames(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws IOException {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        requireNonNull(caseData);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated = searchCasesDataService.updateApplicantAndChildNames(objectMapper, caseDataUpdated);

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
        if (caseDataUpdated.get("caseTypeOfApplication") != null) {
            caseDataUpdated.put("selectedCaseTypeID", caseDataUpdated.get("caseTypeOfApplication"));
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
        List<Element<FurtherEvidence>> furtherEvidencesList = caseData.getFurtherEvidences();
        List<Element<Correspondence>> correspondenceList = caseData.getCorrespondence();
        List<Element<OtherDocuments>> otherDocumentsList = caseData.getOtherDocuments();
        if (furtherEvidencesList != null) {
            List<Element<FurtherEvidence>> furtherEvidences = furtherEvidencesList.stream()
                    .filter(element -> element.getValue().getRestrictCheckboxFurtherEvidence().contains(restrictToGroup))
                .collect(Collectors.toList());
            caseDataUpdated.put("mainAppDocForTabDisplay", furtherEvidences);

            List<Element<FurtherEvidence>> furtherEvidencesNotConfidential = furtherEvidencesList.stream()
                .filter(element -> !element.getValue().getRestrictCheckboxFurtherEvidence().contains(restrictToGroup))
                .collect(Collectors.toList());
            caseDataUpdated.put("mainAppNotConf", furtherEvidencesNotConfidential);
        }
        if (correspondenceList != null) {
            List<Element<Correspondence>> correspondence = correspondenceList.stream()
                .filter(element -> element.getValue().getRestrictCheckboxCorrespondence().contains(restrictToGroup))
                .collect(Collectors.toList());
            caseDataUpdated.put("correspondenceForTabDisplay", correspondence);

            List<Element<Correspondence>> correspondenceForTabDisplayNotConfidential = correspondenceList.stream()
                .filter(element -> !element.getValue().getRestrictCheckboxCorrespondence().contains(restrictToGroup))
                .collect(Collectors.toList());

            caseDataUpdated.put("corrNotConf", correspondenceForTabDisplayNotConfidential);
        }
        if (otherDocumentsList != null) {

            List<Element<OtherDocuments>> otherDocuments = otherDocumentsList.stream()
                .filter(element -> element.getValue().getRestrictCheckboxOtherDocuments().contains(restrictToGroup))
                .collect(Collectors.toList());
            caseDataUpdated.put("otherDocumentsForTabDisplay", otherDocuments);

            List<Element<OtherDocuments>> otherDocumentsForTabDisplayNotConfidential = otherDocumentsList.stream()
                .filter(element -> !element.getValue().getRestrictCheckboxOtherDocuments().contains(restrictToGroup))
                .collect(Collectors.toList());
            caseDataUpdated.put("otherDocNotConf", otherDocumentsForTabDisplayNotConfidential);

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

    @PostMapping(path = "/draft-an-order-about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to Generate document for draft an order")
    public AboutToStartOrSubmitCallbackResponse draftAnOrderAboutToStartEventCallback(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) throws Exception {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails caseDetails = uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails
            .builder().caseData(caseData).build();
        GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(authorisation, caseDetails, draftAnOrder);
        Document document = Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName(draftAnOrderFile).build();
        caseDataUpdated.put("previewDraftAnOrderDocument",document);
        log.info("*** case details {} ***", caseDetails);
        log.info("*** generate document info {} ***", generatedDocumentInfo);
        log.info("*** document {} ***", document);
        log.info("*** caseDataUpdated {} ***", caseDataUpdated);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/draft-an-order-mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to Generate document for draft an order")
    public AboutToStartOrSubmitCallbackResponse draftAnOrderMidEventCallback(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) throws Exception {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("previewDraftAnOrder","\n"
            + "<b>Non-molestation order</b>\n"
            + "Section 42 Family Law Act 1996\n" + "\n" + "Ordered on 4 May 2022 by Her Honour Judge Evans\n" + "\n"
            + "Family court sitting at: Maidstone family court\n" + "\n" + "<b>People in the case</b>\n" + "\n"
            + "Applicant: Rebecca Travis\n" + "Applicant reference: abcd6789\n" + "\n"
            + "Respondent: James Smith born 31 October 1984     \n" + "Respondent reference: efgh4567 \n" + "\n"
            + "\n" + "\n" + "\n"
            + "<b>Important notice to the respondent, James Smith born 31 October 1984, of 3 Cherry Close, Tonbridge, Kent TN9 1TT</b>\n"
            + "\n" + "You must obey this order. You should read it carefully. \n" + "\n"
            + "If you do not understand anything in this order, you should go to a solicitor, legal advice centre or Citizens Advice. \n"
            + "\n" + "You have a right to apply to the court to change or cancel the order. \n" + "\n"
            + "<b>Warning: If, without reasonable excuse, you do anything which you are forbidden from doing by this order, "
            + "you will be committing a criminal offence and liable on conviction to a term of imprisonment not "
            + "exceeding 5 years or to a fine or to both.\n"
            + "Alternatively, if you do not obey this order, you will be guilty of contempt of court and you may be "
            + "sent to prison, be fined, or have your assets seized.</b>\n"
            + "\n" + "\n" + "\n" + "By consent\n" + "The court orders:\n" + "\n"
            + "The respondent James Smith must not use or threaten violence against the applicant Rebecca Travis, and "
            + "must not instruct, encourage or in any way suggest that any other person should do so. \n"
            + "\n" + "This order applies to the respondent once it is personally served on them or once they have been "
            + "made aware of it.\n"
            + "\n" + "This order applies until: 4 May 2023 1pm\n" + "\n"
            + "The respondent has the right to apply to the court at any time to change or cancel this order. If the "
            + "respondent intends to rely on any evidence to support their application, this must be provided in writing"
            + " to the applicant or applicant’s solicitors in advance. \n"
            + "\n" + "If the respondent intends to oppose the order at the next hearing, they must notify the court in "
            + "advance that they intend to attend the hearing and oppose the order. If the respondent does not notify "
            + "the court, the court may decide that the applicant or applicant’s solicitor does not need to attend the "
            + "next hearing, and at the next hearing may make an order to extend the injunction. \n"
            + "\n" + "A further hearing will take place on <b>20 May 2022 10am</b> at Maidstone family court, Medway "
            + "county and family court, 9-11 The Brook, Chatham, Kent ME 4JZ\n"
            + "\n" + "It is estimated the hearing will last: 1 hour\n"
            + "If the respondent does not attend this hearing at this time, the court may make an order in their absence.\n"
            + "\n" + "The order is made without notice to the respondent.\n" + "\n" + "\n" + "\n" + "\n" + "\n"
            + "Where the court has made a non-molestation order, a copy of the order must be delivered to the officer "
            + "in charge of the police station for the applicant’s address, or such other police station as the court "
            + "may specify, and must be accompanied by a statement showing that the respondent has been served with the "
            + "order or informed of its terms: see FPR 2010, rule 10.10\n"
            + "\n" + "\n" + "\n" + "\n" + "<b>Note to the arresting officer</b>\n"
            + "Under section 42A of the Family Law Act 1996, breach of a non-molestation order is a criminal offence "
            + "punishable by up to 5 years’ imprisonment. It is an arrestable offence and it is not necessary to obtain a warrant.\n"
            + "\n" + "A person who without reasonable excuse does anything that they are prohibited from doing by a "
            + "non-molestation order is guilty of an offence.\n"
            + "Family Law Act 1996, section 42A(1).\n" + "\n" + "\n" + "\n" + "\n" + "\n" + "\n" + "\n" + "\n");
        log.info("*** caseDataUpdated {} ***", caseDataUpdated);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}


