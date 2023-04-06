package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.Correspondence;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.GatekeepingDetails;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.CourtSealFinderService;
import uk.gov.hmcts.reform.prl.services.ExampleService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UpdatePartyDetailsService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.GatekeepingDetailsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.workflows.ApplicationConsiderationTimetableValidationWorkflow;
import uk.gov.hmcts.reform.prl.workflows.ValidateMiamApplicationOrExemptionWorkflow;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_CASE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_OR_RESPONDENT_CASE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATE_AND_TIME_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COLON_SEPERATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_SEAL_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDICIAL_REVIEW_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PENDING_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RETURN_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBMITTED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WITHDRAWN_STATE;
import static uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts.restrictToGroup;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getCaseData;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CallbackController {
    public static final String C100_DEFAULT_BASE_LOCATION_NAME = "STOKE ON TRENT TRIBUNAL HEARING CENTRE";
    public static final String C100_DEFAULT_BASE_LOCATION_ID = "283922";
    public static final String C100_DEFAULT_REGION_NAME = "Midlands";
    public static final String C100_DEFAULT_REGION_ID = "2";
    private final CaseEventService caseEventService;
    private final ApplicationConsiderationTimetableValidationWorkflow applicationConsiderationTimetableValidationWorkflow;
    private final ExampleService exampleService;
    private final OrganisationService organisationService;
    private final ValidateMiamApplicationOrExemptionWorkflow validateMiamApplicationOrExemptionWorkflow;
    private final SolicitorEmailService solicitorEmailService;
    private final CaseWorkerEmailService caseWorkerEmailService;

    private final ObjectMapper objectMapper;
    private final AllTabServiceImpl allTabsService;
    private final CaseSummaryTabService caseSummaryTab;
    private final UserService userService;
    private final DocumentGenService documentGenService;
    private final SendgridService sendgridService;
    private final C100JsonMapper c100JsonMapper;
    private final AuthTokenGenerator authTokenGenerator;
    private final CourtFinderService courtLocatorService;
    private final LocationRefDataService locationRefDataService;
    private final UpdatePartyDetailsService updatePartyDetailsService;
    private final PaymentRequestService paymentRequestService;
    private final CourtSealFinderService courtSealFinderService;
    private final ConfidentialityTabService confidentialityTabService;
    private final LaunchDarklyClient launchDarklyClient;
    private final RefDataUserService refDataUserService;
    private final GatekeepingDetailsService gatekeepingDetailsService;

    @PostMapping(path = "/validate-application-consideration-timetable", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(summary = "Callback to validate application consideration timetable. Returns error messages if validation fails.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<CallbackResponse> validateApplicationConsiderationTimetable(
        @RequestBody CallbackRequest callbackRequest
    ) throws WorkflowException {
        WorkflowResult workflowResult = applicationConsiderationTimetableValidationWorkflow.run(callbackRequest);

        return ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .errors(workflowResult.getErrors())
                .build()
        );
    }

    @PostMapping(path = "/validate-miam-application-or-exemption", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to confirm that a MIAM has been attended or applicant is exempt. Returns error message if confirmation fails")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<CallbackResponse> validateMiamApplicationOrExemption(
        @RequestBody CallbackRequest callbackRequest
    ) throws WorkflowException {
        WorkflowResult workflowResult = validateMiamApplicationOrExemptionWorkflow.run(callbackRequest);

        return ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .errors(workflowResult.getErrors())
                .build()

        );
    }

    @PostMapping(path = "/generate-save-draft-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to generate and store document")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse generateAndStoreDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody @Parameter(name = "CaseData") CallbackRequest request
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
    @Operation(description = "Callback to Generate document after submit application")
    public AboutToStartOrSubmitCallbackResponse prePopulateCourtDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        Court closestChildArrangementsCourt = courtLocatorService
            .getNearestFamilyCourt(caseData);
        Optional<CourtEmailAddress> courtEmailAddress = closestChildArrangementsCourt == null ? Optional.empty() : courtLocatorService
            .getEmailAddress(closestChildArrangementsCourt);
        if (courtEmailAddress.isPresent()) {
            log.info("Found court email for case id {}", caseData.getId());
            caseDataUpdated.put("localCourtAdmin", List.of(
                Element.<LocalCourtAdminEmail>builder().value(LocalCourtAdminEmail.builder().email(courtEmailAddress.get().getAddress()).build())
                    .build()));
        } else {
            log.info("Court email not found for case id {}", caseData.getId());
        }
        List<DynamicListElement> courtList = locationRefDataService.getCourtLocations(authorisation);
        caseDataUpdated.put("courtList", DynamicList.builder().value(DynamicListElement.EMPTY).listItems(courtList)
            .build());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/generate-document-submit-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Issue and send to local court")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse generateDocumentSubmitApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put(
            CASE_DATE_AND_TIME_SUBMITTED_FIELD,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime)
        );
        caseData = caseData
            .toBuilder()
            .applicantsConfidentialDetails(
                confidentialityTabService
                    .getConfidentialApplicantDetails(
                        caseData.getApplicants().stream()
                            .map(
                                Element::getValue)
                            .collect(
                                Collectors.toList())))
            .childrenConfidentialDetails(confidentialityTabService.getChildrenConfidentialDetails(
                caseData.getChildren()
                    .stream()
                    .map(Element::getValue)
                    .collect(
                        Collectors.toList()))).state(
                State.SUBMITTED_NOT_PAID)
            .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
            .build();

        Map<String, Object> map = documentGenService.generateDocuments(authorisation, caseData);
        // updating Summary tab to update case status
        caseDataUpdated.putAll(caseSummaryTab.updateTab(caseData));
        caseDataUpdated.putAll(map);

        if (CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
            // updating Summary tab to update case status
            caseDataUpdated.putAll(caseSummaryTab.updateTab(caseData));
            caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));
            caseDataUpdated.putAll(documentGenService.generateDraftDocuments(authorisation, caseData));
        }

        //Assign default court to all c100 cases for work allocation.
        caseDataUpdated.put("caseManagementLocation", CaseManagementLocation.builder()
            .region(C100_DEFAULT_REGION_ID)
            .baseLocation(C100_DEFAULT_BASE_LOCATION_ID).regionName(C100_DEFAULT_REGION_NAME)
            .baseLocationName(C100_DEFAULT_BASE_LOCATION_NAME).build());

        PaymentServiceResponse paymentServiceResponse = paymentRequestService.createServiceRequestFromCcdCallack(
            callbackRequest,
            authorisation
        );
        caseDataUpdated.put(
            "paymentServiceRequestReferenceNumber",
            paymentServiceResponse.getServiceRequestReference()
        );
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/amend-court-details/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Issue and send to local court")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse amendCourtAboutToStart(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) {

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<DynamicListElement> courtList = locationRefDataService.getCourtLocations(authorisation);
        caseDataUpdated.put("courtList", DynamicList.builder().value(DynamicListElement.EMPTY).listItems(courtList)
            .build());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/amend-court-details/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Issue and send to local court")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse amendCourtAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        String baseLocationId = caseData.getCourtList().getValue().getCode().split(COLON_SEPERATOR)[0];
        Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId(baseLocationId, authorisation);
        caseDataUpdated.putAll(CaseUtils.getCourtDetails(courtVenue, baseLocationId));
        if (courtVenue.isPresent()) {
            String courtSeal = courtSealFinderService.getCourtSeal(courtVenue.get().getRegionId());
            caseDataUpdated.put(COURT_SEAL_FIELD, courtSeal);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/update-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to refresh the tabs")
    @SecurityRequirement(name = "Bearer Authentication")
    public void updateApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        allTabsService.updateAllTabs(caseData);
    }

    @PostMapping(path = "/case-withdrawn-email-notification", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Send Email Notification on Case Withdraw")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse sendEmailNotificationOnCaseWithdraw(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
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
                sendWithdrawEmails(caseData, userDetails, caseDetails);
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

    private void sendWithdrawEmails(CaseData caseData, UserDetails userDetails, CaseDetails caseDetails) {
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
            caseWorkerEmailService.sendWithdrawApplicationEmailToLocalCourt(
                caseDetails,
                caseData.getCourtEmailAddress());
        }
    }

    @PostMapping(path = "/send-to-gatekeeper", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Send Email Notification on Send to gatekeeper ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse sendToGatekeeper(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("Gatekeeping details for the case id : {}", caseData.getId());

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseWorkerEmailService.sendEmailToGateKeeper(caseDetails);

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        GatekeepingDetails gatekeepingDetails = gatekeepingDetailsService.getGatekeepingDetails(caseDataUpdated,
                                                                                                caseData.getLegalAdviserList(), refDataUserService);
        caseData = caseData.toBuilder().gatekeepingDetails(gatekeepingDetails).build();

        caseDataUpdated.put("gatekeepingDetails", gatekeepingDetails);

        Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);
        caseDataUpdated.putAll(allTabsFields);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/resend-rpa", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Resend case data json to RPA")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse resendNotificationToRpa(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws IOException {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        requireNonNull(caseData);
        sendgridService.sendEmail(c100JsonMapper.map(caseData));
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/update-party-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Update Applicants, Children and Respondents details for future processing")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse updatePartyDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(updatePartyDetailsService.updateApplicantAndChildNames(callbackRequest))
            .build();
    }

    @PostMapping(path = "/about-to-submit-case-creation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Copy fl401 case name to C100 Case name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse aboutToSubmitCaseCreation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        //Added for Case linking
        if (caseDataUpdated.get(APPLICANT_CASE_NAME) != null) {
            caseDataUpdated.put("caseNameHmctsInternal", caseDataUpdated.get(APPLICANT_CASE_NAME));
        }
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        // Updating the case name for FL401
        if (caseDataUpdated.get(APPLICANT_OR_RESPONDENT_CASE_NAME) != null) {
            caseDataUpdated.put(APPLICANT_CASE_NAME, caseDataUpdated.get(APPLICANT_OR_RESPONDENT_CASE_NAME));
            //Added for Case linking
            caseDataUpdated.put("caseNameHmctsInternal", caseDataUpdated.get(APPLICANT_OR_RESPONDENT_CASE_NAME));
        }
        if (caseDataUpdated.get("caseTypeOfApplication") != null) {
            caseDataUpdated.put("selectedCaseTypeID", caseDataUpdated.get("caseTypeOfApplication"));
        }

        // Saving the logged-in Solicitor and Org details for the docs..
        return AboutToStartOrSubmitCallbackResponse.builder().data(getSolicitorDetails(
            authorisation,
            caseDataUpdated,
            caseData
        )).build();
    }

    @PostMapping(path = "/fl401-add-case-number", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for add case number submit event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse addCaseNumberSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
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
            || JUDICIAL_REVIEW_STATE.equalsIgnoreCase(eachState);
    }

    @PostMapping(path = "/copy-manage-docs-for-tabs", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Copy manage docs for tabs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse copyManageDocsForTabs(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
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

    private Map<String, Object> getSolicitorDetails(String authorisation, Map<String, Object> caseDataUpdated, CaseData caseData) {

        log.info("Fetching the user and Org Details ");
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            Optional<Organisations> userOrganisation = organisationService.findUserOrganisation(authorisation);
            caseDataUpdated.put("caseSolicitorName", userDetails.getFullName());
            if (userOrganisation.isPresent()) {
                log.info("Got the Org Details");
                caseDataUpdated.put("caseSolicitorOrgName", userOrganisation.get().getName());
                if (launchDarklyClient.isFeatureEnabled("share-a-case")) {
                    OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                          .organisationID(userOrganisation.get().getOrganisationIdentifier())
                                          .organisationName(userOrganisation.get().getName())
                                          .build())
                        .orgPolicyReference(caseData.getApplicantOrganisationPolicy().getOrgPolicyReference())
                        .orgPolicyCaseAssignedRole(caseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole())
                        .build();
                    caseDataUpdated.put("applicantOrganisationPolicy", applicantOrganisationPolicy);
                }
            }
            log.info("SUCCESSFULLY fetched user and Org Details ");
        } catch (Exception e) {
            log.error("Error while fetching User or Org details for the logged in user ", e);
        }

        return caseDataUpdated;

    }

}

