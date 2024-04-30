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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.DocumentCategoryEnum;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.events.TransferToAnotherCourtEvent;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.Correspondence;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.GatekeepingDetails;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.RoleAssignmentDto;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.services.AmendCourtService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeFileUploadService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UpdatePartyDetailsService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.GatekeepingDetailsService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_CASE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_OR_RESPONDENT_CASE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_CREATED_BY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATE_AND_TIME_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.GATEKEEPING_JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_JUDGE_OR_LEGAL_ADVISOR_GATEKEEPING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDICIAL_REVIEW_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PENDING_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RETURN_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBMITTED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.VERIFY_CASE_NUMBER_ADDED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WITHDRAWN_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.CREATE_URGENT_CASES_FLAG;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.TASK_LIST_V2_FLAG;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.TASK_LIST_V3_FLAG;
import static uk.gov.hmcts.reform.prl.enums.Event.SEND_TO_GATEKEEPER;
import static uk.gov.hmcts.reform.prl.enums.State.SUBMITTED_PAID;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
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
    public static final String COURT_LIST = "courtList";
    private static final String CONFIRMATION_HEADER = "# Case transferred to another court ";
    private static final String CONFIRMATION_BODY_PREFIX = "The case has been transferred to ";
    private static final String CONFIRMATION_BODY_SUFFIX = " \n\n Local court admin have been notified ";
    public static final String TASK_LIST_VERSION = "taskListVersion";
    private final CaseEventService caseEventService;
    private final ApplicationConsiderationTimetableValidationWorkflow applicationConsiderationTimetableValidationWorkflow;
    private final OrganisationService organisationService;
    private final ValidateMiamApplicationOrExemptionWorkflow validateMiamApplicationOrExemptionWorkflow;

    private final ObjectMapper objectMapper;
    private final AllTabServiceImpl allTabsService;
    private final CaseSummaryTabService caseSummaryTab;
    private final UserService userService;
    private final DocumentGenService documentGenService;
    private final SendgridService sendgridService;
    private final C100JsonMapper c100JsonMapper;
    private final CourtFinderService courtLocatorService;
    private final LocationRefDataService locationRefDataService;
    private final UpdatePartyDetailsService updatePartyDetailsService;
    private final PaymentRequestService paymentRequestService;
    private final ConfidentialityTabService confidentialityTabService;
    private final LaunchDarklyClient launchDarklyClient;
    private final RefDataUserService refDataUserService;
    private final GatekeepingDetailsService gatekeepingDetailsService;
    private final AuthorisationService authorisationService;
    private final AmendCourtService amendCourtService;
    private final EventService eventPublisher;
    private final ManageDocumentsService manageDocumentsService;

    private final RoleAssignmentService roleAssignmentService;
    private final RoleAssignmentApi roleAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;

    private final MiamPolicyUpgradeService miamPolicyUpgradeService;
    private final MiamPolicyUpgradeFileUploadService miamPolicyUpgradeFileUploadService;
    private final SystemUserService systemUserService;

    @PostMapping(path = "/validate-application-consideration-timetable", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(summary = "Callback to validate application consideration timetable. Returns error messages if validation fails.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<CallbackResponse> validateApplicationConsiderationTimetable(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) throws WorkflowException {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            WorkflowResult workflowResult = applicationConsiderationTimetableValidationWorkflow.run(callbackRequest);
            return ok(
                AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(workflowResult.getErrors())
                    .build()
            );
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/validate-miam-application-or-exemption", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to confirm that a MIAM has been attended or applicant is exempt. Returns error message if confirmation fails")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<CallbackResponse> validateMiamApplicationOrExemption(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) throws WorkflowException {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            WorkflowResult workflowResult = validateMiamApplicationOrExemptionWorkflow.run(callbackRequest);

            return ok(
                AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(workflowResult.getErrors())
                    .build()

            );
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/generate-save-draft-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to generate and store document")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse generateAndStoreDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody @Parameter(name = "CaseData") CallbackRequest request
    ) throws Exception {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(request.getCaseDetails(), objectMapper);

            if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                caseData = buildTypeOfApplicationCaseData(caseData);
            }

            Map<String, Object> caseDataUpdated = request.getCaseDetails().getData();
            if (TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())
                && isNotEmpty(caseData.getMiamPolicyUpgradeDetails())) {
                caseData = miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(caseData, caseDataUpdated);
            }
            // Generate draft documents and set to casedataupdated.
            caseDataUpdated.putAll(documentGenService.generateDraftDocuments(authorisation, caseData));
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
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
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {

        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
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
            caseDataUpdated.put(COURT_LIST, DynamicList.builder().value(DynamicListElement.EMPTY).listItems(courtList)
                .build());
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/generate-document-submit-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Issue and send to local court")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse generateDocumentSubmitApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
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
                                .toList()))
                .childrenConfidentialDetails(confidentialityTabService.getChildrenConfidentialDetails(
                    caseData)).state(
                    State.SUBMITTED_NOT_PAID)
                .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
                .build();

            if (C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))
                && TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())
                && isNotEmpty(caseData.getMiamPolicyUpgradeDetails())) {
                caseData = populateMiamPolicyUpgradeDetails(caseData, caseDataUpdated);
            }

            Map<String, Object> map = documentGenService.generateDocuments(authorisation, caseData);
            // updating Summary tab to update case status
            caseDataUpdated.putAll(caseSummaryTab.updateTab(caseData));
            caseDataUpdated.putAll(map);

            if (CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
                // updating Summary tab to update case status
                caseDataUpdated.putAll(caseSummaryTab.updateTab(caseData));
                caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));
                caseDataUpdated.putAll(documentGenService.generateDraftDocuments(authorisation, caseData));
                //Update version V2 here to get latest data refreshed in tabs
                if (launchDarklyClient.isFeatureEnabled(TASK_LIST_V3_FLAG)) {
                    caseDataUpdated.put(TASK_LIST_VERSION, TASK_LIST_VERSION_V3);
                } else if (launchDarklyClient.isFeatureEnabled(TASK_LIST_V2_FLAG)) {
                    caseDataUpdated.put(TASK_LIST_VERSION, TASK_LIST_VERSION_V2);
                }
            } else if (CaseCreatedBy.COURT_ADMIN.equals(caseData.getCaseCreatedBy())) {
                caseDataUpdated.put("caseStatus", CaseStatus.builder()
                    .state(SUBMITTED_PAID.getLabel())
                    .build());
                caseDataUpdated.put(STATE_FIELD, SUBMITTED_PAID.getValue());
            } else {
                PaymentServiceResponse paymentServiceResponse = paymentRequestService.createServiceRequestFromCcdCallack(
                    callbackRequest,
                    authorisation
                );
                caseDataUpdated.put(
                    "paymentServiceRequestReferenceNumber",
                    paymentServiceResponse.getServiceRequestReference()
                );
            }
            //Assign default court to all c100 cases for work allocation.
            caseDataUpdated.put("caseManagementLocation", CaseManagementLocation.builder()
                .region(C100_DEFAULT_REGION_ID)
                .baseLocation(C100_DEFAULT_BASE_LOCATION_ID).regionName(C100_DEFAULT_REGION_NAME)
                .baseLocationName(C100_DEFAULT_BASE_LOCATION_NAME).build());
            caseDataUpdated.put("caseFlags", Flags.builder().build());
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private CaseData populateMiamPolicyUpgradeDetails(CaseData caseData, Map<String, Object> caseDataUpdated) {
        caseData = miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(caseData, caseDataUpdated);
        caseData = miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(
            caseData,
            systemUserService.getSysUserToken()
        );
        allTabsService.getNewMiamPolicyUpgradeDocumentMap(caseData, caseDataUpdated);
        return caseData;
    }

    @PostMapping(path = "/transfer-court/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Issue and send to local court")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse amendCourtAboutToStart(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) {

        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            List<DynamicListElement> courtList;
            if (Event.TRANSFER_TO_ANOTHER_COURT.getId().equalsIgnoreCase(callbackRequest.getEventId())) {
                courtList = locationRefDataService.getFilteredCourtLocations(authorisation);
            } else {
                courtList = locationRefDataService.getCourtLocations(authorisation);
            }
            caseDataUpdated.put(COURT_LIST, DynamicList.builder().value(DynamicListElement.EMPTY).listItems(courtList)
                .build());
            caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }


    @PostMapping(path = "/transfer-court/validate-court-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to validate court fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Child details are fetched"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse validateCourtFields(
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        List<String> errorList = new ArrayList<>();
        if (amendCourtService.validateCourtFields(caseData, errorList)) {
            return uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.builder()
                .errors(errorList)
                .build();
        }
        return uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.builder()
            .data(caseData)
            .build();
    }

    @PostMapping(path = "/transfer-court/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Issue and send to local court")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse amendCourtAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            amendCourtService.handleAmendCourtSubmission(authorisation, callbackRequest, caseDataUpdated);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/update-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to refresh the tabs")
    @SecurityRequirement(name = "Bearer Authentication")
    public void updateApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            allTabsService.updateAllTabsIncludingConfTab(String.valueOf(callbackRequest.getCaseDetails().getId()));
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/case-withdrawn-about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Send Email Notification on Case Withdraw")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse caseWithdrawAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            List<CaseEventDetail> eventsForCase = caseEventService.findEventsForCase(String.valueOf(caseData.getId()));

            Optional<String> previousState = eventsForCase.stream().map(CaseEventDetail::getStateId)
                .filter(
                    CallbackController::getPreviousState).findFirst();

            List<String> stateList = List.of(DRAFT_STATE, "CLOSED",
                                             PENDING_STATE,
                                             SUBMITTED_STATE, RETURN_STATE
            );
            WithdrawApplication withDrawApplicationData = caseData.getWithDrawApplicationData();
            Optional<YesOrNo> withdrawApplication = ofNullable(withDrawApplicationData.getWithDrawApplication());
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            updateTabsOrWithdrawFlag(caseData, previousState, stateList, withdrawApplication, caseDataUpdated);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private void updateTabsOrWithdrawFlag(CaseData caseData, Optional<String> previousState, List<String> stateList,
                                          Optional<YesOrNo> withdrawApplication, Map<String, Object> caseDataUpdated) {
        if ((withdrawApplication.isPresent() && Yes.equals(withdrawApplication.get()))) {
            if (previousState.isPresent() && !stateList.contains(previousState.get())) {
                caseDataUpdated.put("isWithdrawRequestSent", "Pending");
            } else {
                if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                    // Refreshing the page in the same event. Hence no external event call needed.
                    // Getting the tab fields and add it to the casedetails..
                    Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);
                    caseDataUpdated.putAll(allTabsFields);
                }
                if (!State.AWAITING_RESUBMISSION_TO_HMCTS.equals(caseData.getState())) {
                    caseDataUpdated.put("state", WITHDRAWN_STATE);
                    caseData = caseData.toBuilder().state(State.CASE_WITHDRAWN).build();
                    caseDataUpdated.putAll(caseSummaryTab.updateTab(caseData));
                }
            }
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
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = getCaseData(callbackRequest.getCaseDetails(), objectMapper);

            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

            GatekeepingDetails gatekeepingDetails = gatekeepingDetailsService.getGatekeepingDetails(
                caseDataUpdated,
                caseData.getLegalAdviserList(),
                refDataUserService
            );
            caseData = caseData.toBuilder().gatekeepingDetails(gatekeepingDetails).build();

            caseDataUpdated.put("gatekeepingDetails", gatekeepingDetails);

            Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);
            caseDataUpdated.putAll(allTabsFields);
            if (caseDataUpdated.get(IS_JUDGE_OR_LEGAL_ADVISOR_GATEKEEPING) != null
                && (gatekeepingDetails.getJudgeName() != null
                || (gatekeepingDetails.getLegalAdviserList() != null
                && CollectionUtils.isNotEmpty(gatekeepingDetails.getLegalAdviserList().getListItems())))) {
                RoleAssignmentDto roleAssignmentDto = RoleAssignmentDto.builder()
                    .judicialUser(gatekeepingDetails.getJudgeName())
                    .legalAdviserList(gatekeepingDetails.getLegalAdviserList())
                    .build();

                roleAssignmentService.createRoleAssignment(
                    authorisation,
                    callbackRequest.getCaseDetails(),
                    roleAssignmentDto,
                    SEND_TO_GATEKEEPER.getName(),
                    false,
                    GATEKEEPING_JUDGE_ROLE
                );
            }
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
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
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) throws IOException {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            requireNonNull(caseData);
            sendgridService.sendEmail(c100JsonMapper.map(caseData));
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
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
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest, authorisation))
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
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
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            //Added for Case linking
            if (caseDataUpdated.get(APPLICANT_CASE_NAME) != null) {
                caseDataUpdated.put("caseNameHmctsInternal", caseDataUpdated.get(APPLICANT_CASE_NAME));
            }
            // Updating the case name for FL401
            if (caseDataUpdated.get(APPLICANT_OR_RESPONDENT_CASE_NAME) != null) {
                caseDataUpdated.put(APPLICANT_CASE_NAME, caseDataUpdated.get(APPLICANT_OR_RESPONDENT_CASE_NAME));
                //Added for Case linking
                caseDataUpdated.put("caseNameHmctsInternal", caseDataUpdated.get(APPLICANT_OR_RESPONDENT_CASE_NAME));
            }
            if (caseDataUpdated.get(CASE_TYPE_OF_APPLICATION) != null) {
                caseDataUpdated.put("selectedCaseTypeID", caseDataUpdated.get(CASE_TYPE_OF_APPLICATION));
                if (isEmpty(caseDataUpdated.get(TASK_LIST_VERSION))) {
                    setTaskListVersion(caseDataUpdated);
                }
            }
            populateCaseCreatedByField(authorisation,caseDataUpdated);
            // Saving the logged-in Solicitor and Org details for the docs..
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            return AboutToStartOrSubmitCallbackResponse.builder().data(getSolicitorDetails(
                authorisation,
                caseDataUpdated,
                caseData
            )).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/validate-urgent-case-creation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Copy fl401 case name to C100 Case name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse validateUrgentCaseCreation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            if (!launchDarklyClient.isFeatureEnabled(CREATE_URGENT_CASES_FLAG)) {
                return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of(
                    "Sorry unable to create any urgent cases now")).build();
            } else {
                return AboutToStartOrSubmitCallbackResponse.builder().data(callbackRequest.getCaseDetails().getData()).build();
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private void setTaskListVersion(Map<String, Object> caseDataUpdated) {
        if (C100_CASE_TYPE.equals(caseDataUpdated.get(CASE_TYPE_OF_APPLICATION))) {
            if (launchDarklyClient.isFeatureEnabled(TASK_LIST_V3_FLAG)) {
                caseDataUpdated.put(TASK_LIST_VERSION, TASK_LIST_VERSION_V3);
            } else if (launchDarklyClient.isFeatureEnabled(TASK_LIST_V2_FLAG)) {
                caseDataUpdated.put(TASK_LIST_VERSION, TASK_LIST_VERSION_V2);
            }
        }
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
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            List<CaseEventDetail> eventsForCase = caseEventService.findEventsForCase(String.valueOf(callbackRequest.getCaseDetails().getId()));

            Optional<String> previousState = eventsForCase.stream()
                .map(CaseEventDetail::getStateId)
                .findFirst();
            previousState.ifPresent(s -> caseDataUpdated.put(
                VERIFY_CASE_NUMBER_ADDED,
                SUBMITTED_PAID.getValue().equalsIgnoreCase(s) ? Yes : No
            ));
            caseDataUpdated.put(ISSUE_DATE_FIELD, LocalDate.now());
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated)
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
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
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            List<Element<FurtherEvidence>> furtherEvidencesList = caseData.getFurtherEvidences();
            List<Element<Correspondence>> correspondenceList = caseData.getCorrespondence();
            List<Element<QuarantineLegalDoc>> quarantineDocs = caseData.getDocumentManagementDetails() != null
                    ? caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList()
                    : DocumentManagementDetails.builder().legalProfQuarantineDocsList(new ArrayList<>()).build().getLegalProfQuarantineDocsList();
            if (furtherEvidencesList != null) {
                quarantineDocs.addAll(furtherEvidencesList.stream().map(element -> Element.<QuarantineLegalDoc>builder()
                                .value(QuarantineLegalDoc.builder().document(element.getValue().getDocumentFurtherEvidence())
                                        .documentType(element.getValue().getTypeOfDocumentFurtherEvidence().toString())
                                        .restrictCheckboxCorrespondence(element.getValue().getRestrictCheckboxFurtherEvidence())
                                        .notes(caseData.getGiveDetails())
                                        .categoryId(DocumentCategoryEnum.documentCategoryChecklistEnumValue1.getDisplayedValue())
                                        .build())
                                .id(element.getId()).build())
                        .toList());
            }
            if (correspondenceList != null) {
                quarantineDocs.addAll(correspondenceList.stream().map(element -> Element.<QuarantineLegalDoc>builder()
                                .value(QuarantineLegalDoc.builder().document(element.getValue().getDocumentCorrespondence())
                                        .documentName(element.getValue().getDocumentName())
                                        .restrictCheckboxCorrespondence(element.getValue().getRestrictCheckboxCorrespondence())
                                        .notes(element.getValue().getNotes())
                                        .categoryId(DocumentCategoryEnum.documentCategoryChecklistEnumValue2.getDisplayedValue())
                                        .build())
                                .id(element.getId()).build())
                        .toList());
            }
            List<Element<OtherDocuments>> otherDocumentsList = caseData.getOtherDocuments();
            if (otherDocumentsList != null) {
                quarantineDocs.addAll(otherDocumentsList.stream().map(element -> Element.<QuarantineLegalDoc>builder()
                                .value(QuarantineLegalDoc.builder().document(element.getValue().getDocumentOther())
                                        .documentType(element.getValue().getDocumentTypeOther().toString())
                                        .notes(element.getValue().getNotes())
                                        .documentName(element.getValue().getDocumentName())
                                        .categoryId(DocumentCategoryEnum.documentCategoryChecklistEnumValue3.getDisplayedValue())
                                        .restrictCheckboxCorrespondence(element.getValue().getRestrictCheckboxOtherDocuments())
                                        .build())
                                .id(element.getId()).build())
                        .toList());
            }
            caseData.setDocumentManagementDetails(DocumentManagementDetails.builder()
                    .legalProfQuarantineDocsList(quarantineDocs)
                    .build());
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            caseDataUpdated.put(
                    "legalProfQuarantineDocsList",
                    caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList()
            );
            caseDataUpdated.remove("furtherEvidences");
            caseDataUpdated.remove("correspondence");
            caseDataUpdated.remove("otherDocuments");
            caseDataUpdated.remove("documentCategoryChecklist");
            caseDataUpdated.remove("giveDetails");
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private Map<String, Object> getSolicitorDetails(String authorisation, Map<String, Object> caseDataUpdated, CaseData caseData) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            Optional<Organisations> userOrganisation = organisationService.findUserOrganisation(authorisation);
            caseDataUpdated.put("caseSolicitorName", userDetails.getFullName());
            if (userOrganisation.isPresent()) {
                caseDataUpdated.put("caseSolicitorOrgName", userOrganisation.get().getName());
                if (launchDarklyClient.isFeatureEnabled("share-a-case")) {
                    OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                          .organisationID(userOrganisation.get().getOrganisationIdentifier())
                                          .organisationName(userOrganisation.get().getName())
                                          .build())
                        .build();
                    if (caseData.getApplicantOrganisationPolicy() != null) {
                        applicantOrganisationPolicy = applicantOrganisationPolicy.toBuilder()
                            .orgPolicyReference(caseData.getApplicantOrganisationPolicy().getOrgPolicyReference())
                            .orgPolicyCaseAssignedRole(caseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole())
                            .build();
                    } else {
                        applicantOrganisationPolicy = applicantOrganisationPolicy.toBuilder()
                            .orgPolicyReference(StringUtils.EMPTY)
                            .orgPolicyCaseAssignedRole("[APPLICANTSOLICITOR]")
                            .build();
                    }
                    caseDataUpdated.put("applicantOrganisationPolicy", applicantOrganisationPolicy);
                }
            }
        } catch (Exception e) {
            log.error("Error while fetching User or Org details for the logged in user ", e);
        }

        return caseDataUpdated;

    }


    @PostMapping(path = "/transfer-court/transfer-court-confirmation",
        consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to create confirmation of transfer court ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = uk.gov.hmcts.reform.ccd.client.model.CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<SubmittedCallbackResponse> transferCourtConfirmation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseDetails caseDetails
                = allTabsService.updateAllTabsIncludingConfTab(String.valueOf(callbackRequest.getCaseDetails().getId()));
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        TransferToAnotherCourtEvent event =
            prepareTransferToAnotherCourtEvent(authorisation, caseData,
                                               Event.TRANSFER_TO_ANOTHER_COURT.getName()
            );
        eventPublisher.publishEvent(event);
        return ok(SubmittedCallbackResponse.builder().confirmationHeader(
            CONFIRMATION_HEADER).confirmationBody(
            CONFIRMATION_BODY_PREFIX + caseData.getCourtName()
                + CONFIRMATION_BODY_SUFFIX
        ).build());
    }

    private TransferToAnotherCourtEvent prepareTransferToAnotherCourtEvent(String authorisation, CaseData newCaseData,
                                                                           String typeOfEvent) {
        return TransferToAnotherCourtEvent.builder()
            .authorisation(authorisation)
            .caseData(newCaseData)
            .typeOfEvent(typeOfEvent)
            .build();
    }


    @PostMapping(path = "/attach-scan-docs/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for add case number submit event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse attachScanDocsWaChange(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            QuarantineLegalDoc quarantineLegalDocForBulkScannedDocument = QuarantineLegalDoc.builder()
                .isConfidential(Yes)
                .build();
            manageDocumentsService.setFlagsForWaTask(
                caseData,
                caseDataUpdated,
                COURT_STAFF,
                quarantineLegalDocForBulkScannedDocument
            );
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated)
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }


    @PostMapping(path = "/fetchRoleAssignmentForUser",
        consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to allocate judge ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = uk.gov.hmcts.reform.ccd.client.model.CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse fetchRoleAssignmentForUser(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (!roleAssignmentService.validateIfUserHasRightRoles(
            authorisation,
            callbackRequest
        )) {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of(
                "The selected user does not have right roles to assign this case")).build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void populateCaseCreatedByField(String authorisation, Map<String, Object> caseDataUpdated) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        List<String> roles = userDetails.getRoles();
        if (launchDarklyClient.isFeatureEnabled(ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY)) {
            RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentApi.getRoleAssignments(
                authorisation,
                authTokenGenerator.generate(),
                null,
                userDetails.getId()
            );
            roles = CaseUtils.mapAmUserRolesToIdamRoles(roleAssignmentServiceResponse, authorisation, userDetails);
        }
        log.info("list of roles {}", roles);
        boolean isCourtStaff = roles.stream().anyMatch(ROLES::contains);
        if (isCourtStaff) {
            caseDataUpdated.put(CASE_CREATED_BY,CaseCreatedBy.COURT_ADMIN);
        }
    }
}

