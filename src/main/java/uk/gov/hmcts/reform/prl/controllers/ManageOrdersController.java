package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrLegalAdvisorCheckEnum;
import uk.gov.hmcts.reform.prl.exception.InvalidClientException;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.roleassignment.RoleAssignmentDto;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.AmendOrderService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CustomOrderService;
import uk.gov.hmcts.reform.prl.services.DocumentSealingService;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassDateTimeService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.AutomatedHearingUtils;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils;
import uk.gov.hmcts.reform.prl.utils.TaskUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.ws.rs.core.HttpHeaders;

import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CLIENT_CONTEXT_HEADER_PARAMETER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CUSTOM_ORDER_DOC;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_INVOKED_FROM_TASK;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LOGGED_IN_USER_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PREVIEW_ORDER_DOC;
import static uk.gov.hmcts.reform.prl.enums.State.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.prl.enums.State.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.amendOrderUnderSlipRule;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.createAnOrder;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.createCustomOrder;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.servedSavedOrders;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.uploadAnOrder;
import static uk.gov.hmcts.reform.prl.services.ManageOrderService.cleanUpSelectedManageOrderOptions;
import static uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils.getErrorForOccupationScreen;
import static uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils.getErrorsForOrdersProhibitedForC100FL401;
import static uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils.getHearingScreenValidations;
import static uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils.getHearingScreenValidationsForSdo;
import static uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils.isHearingPageNeeded;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ManageOrdersController {
    public static final String THIS_FEATURE_IS_NOT_CURRENTLY_AVAILABLE_PLEASE_REFER_TO_HMCTS_GUIDANCE =
        "This feature is not currently available. Please refer to HMCTS guidance.";
    private final ObjectMapper objectMapper;
    private final ManageOrderService manageOrderService;
    private final CustomOrderService customOrderService;
    private final DocumentSealingService documentSealingService;
    private final ManageOrderEmailService manageOrderEmailService;
    private final AmendOrderService amendOrderService;
    private final RefDataUserService refDataUserService;
    private final UserService userService;
    private final HearingDataService hearingDataService;
    private final AuthorisationService authorisationService;
    private final AllTabServiceImpl allTabService;
    private final RoleAssignmentService roleAssignmentService;
    private final HearingService hearingService;
    private final TaskUtils taskUtils;
    private final CafcassDateTimeService cafcassDateTimeService;

    public static final String ORDERS_NEED_TO_BE_SERVED = "ordersNeedToBeServed";

    @PostMapping(path = "/populate-preview-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to show preview order in next screen for upload order")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse populatePreviewOrderWhenOrderUploaded(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestHeader(value = CLIENT_CONTEXT_HEADER_PARAMETER, required = false) String clientContext,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

            // Custom order flow - skip preview rendering here, it will be done on Page 19 with hearing data
            if (caseData.getManageOrdersOptions() != null && caseData.getManageOrdersOptions().equals(createCustomOrder)) {
                // Set loggedInUserType for field show conditions
                String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);
                caseDataUpdated.put(LOGGED_IN_USER_TYPE, loggedInUserType);
                // Copy custom order sub-selections to pre-existing fields and clear irrelevant ones
                manageOrderService.syncCustomOrderFieldsToPreExisting(caseDataUpdated);
                return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
            }

            String language = CaseUtils.getLanguage(clientContext);
            List<String> errorList = ManageOrdersUtils.validateMandatoryJudgeOrMagistrate(caseData, CaseUtils.getLanguage(clientContext));
            errorList.addAll(getErrorForOccupationScreen(caseData, caseData.getCreateSelectOrderOptions(), language));
            if (isNotEmpty(errorList)) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errorList)
                    .build();
            }
            return AboutToStartOrSubmitCallbackResponse.builder().data(manageOrderService.handlePreviewOrder(
                callbackRequest,
                authorisation,
                language
            )).build();
        } else {
            throw (new InvalidClientException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/fetch-order-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to fetch case data and custom order fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order details are fetched"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse prepopulateCaseDetails(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestHeader(value = CLIENT_CONTEXT_HEADER_PARAMETER, required = false) String clientContext,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );

            List<String> errorList = new ArrayList<>();
            if (getErrorsForOrdersProhibitedForC100FL401(
                caseData,
                caseData.getCreateSelectOrderOptions(),
                errorList,
                PrlAppsConstants.ENGLISH
            )) {
                return AboutToStartOrSubmitCallbackResponse.builder().errors(errorList).build();
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(manageOrderService.handleFetchOrderDetails(
                    authorisation,
                    callbackRequest,
                    PrlAppsConstants.ENGLISH,
                    clientContext))
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }

    }


    @PostMapping(path = "/populate-header", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateHeader(
        @RequestBody CallbackRequest callbackRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        return getAboutToStartOrSubmitCallbackResponse(
            callbackRequest,
            authorisation,
            s2sToken,
            caseData -> prepopulateHeaderFields(caseData, authorisation)
        );
    }

    private void prepopulateHeaderFields(Map<String, Object> caseData, String authorisation) {
        caseData.put(IS_INVOKED_FROM_TASK, No);
        caseData.put("dateOrderMade", java.time.LocalDate.now());

        String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);
        if (UserRoles.JUDGE.name().equals(loggedInUserType)) {
            uk.gov.hmcts.reform.idam.client.models.UserDetails userDetails = userService.getUserDetails(authorisation);
            if (userDetails != null && userDetails.getFullName() != null) {
                caseData.put("judgeOrMagistratesLastName", userDetails.getFullName());
                log.info("Pre-populated judge name for manage orders: {}", userDetails.getFullName());
            }
        }
    }


    @PostMapping(path = "/populate-header-task", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header from WA task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateHeaderTask(
        @RequestBody CallbackRequest callbackRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        return getAboutToStartOrSubmitCallbackResponse(callbackRequest,
                                                       authorisation,
                                                       s2sToken,
                                                       updateCaseData -> updateCaseData.put(IS_INVOKED_FROM_TASK, Yes)
        );
    }

    @PostMapping(path = "/manage-orders/populate-from-hearing", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate order fields from selected hearing")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fields populated from hearing"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateFromHearing(
        @RequestBody CallbackRequest callbackRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

            // Populate fields from selected hearing (date, judge name, judge title)
            // Silently handles HMC API failures - preserves existing values on error
            manageOrderService.populateFieldsFromSelectedHearing(authorisation, caseData, caseDataUpdated);

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated)
                .build();
        } else {
            throw new InvalidClientException(INVALID_CLIENT);
        }
    }

    private AboutToStartOrSubmitCallbackResponse getAboutToStartOrSubmitCallbackResponse(CallbackRequest callbackRequest,
                                                                                         String authorisation,
                                                                                         String s2sToken,
                                                                                         Consumer<Map<String, Object>> updateCaseData) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            Map<String, Object> caseDataUpdated = new HashMap<>();
            caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
            caseDataUpdated.put("loggedInUserType", manageOrderService.getLoggedInUserType(authorisation));
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                caseDataUpdated.put(
                    "isInHearingState",
                    (PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue().equals(callbackRequest.getCaseDetails().getState())
                        || DECISION_OUTCOME.getValue().equals(callbackRequest.getCaseDetails().getState())) ? Yes : No
                );
                caseDataUpdated.put(PrlAppsConstants.CAFCASS_OR_CYMRU_NEED_TO_PROVIDE_REPORT, Yes);
                if (Yes.equals(caseData.getIsCafcass())) {
                    caseDataUpdated.put(PrlAppsConstants.CAFCASS_SERVED_OPTIONS, caseData.getManageOrders().getCafcassServedOptions());
                }
            }
            updateCaseData.accept(caseDataUpdated);
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated)
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/case-order-email-notification", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Send Email Notification on Case order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse finalizeOrderSubmissionAndSendNotifications(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        log.info(">>> Submitted callback (case-order-email-notification) called");
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
                = allTabService.getStartAllTabsUpdate(String.valueOf(callbackRequest.getCaseDetails().getId()));
            Map<String, Object> caseDataUpdated = startAllTabsUpdateDataContent.caseDataMap();
            //SNI-4330 fix - this will set state in caseData
            //updating state in caseData so that caseSummaryTab is updated with latest state
            CaseData caseData = startAllTabsUpdateDataContent.caseData();

            // Custom order flow: combine header preview + user content, update the order
            Map<String, Object> callbackData = callbackRequest.getCaseDetails().getData();
            boolean isCustomOrder = copyCustomOrderFieldsFromCallback(callbackData, caseDataUpdated);
            if (isCustomOrder) {
                processCustomOrder(authorisation, caseData, caseDataUpdated);
            }

            // Skip addSealToOrders for custom orders - the combined document isn't CDAM-associated yet
            // (association happens during submitAllTabsUpdate below). sealCustomOrderSynchronously handles sealing after.
            if (!isCustomOrder) {
                try {
                    manageOrderService.addSealToOrders(authorisation, caseData, caseDataUpdated);
                } catch (Exception e) {
                    log.error("Sealing failed for case {}: {}", caseData.getId(), e.getMessage(), e);
                }
            }
            log.info("Notifications to be sent? - {}", caseData.getManageOrders().getMarkedToServeEmailNotification());
            if (Yes.equals(caseData.getManageOrders().getMarkedToServeEmailNotification())) {
                log.info("Preparing to send notifications to parties for case id {}", caseData.getId());
                manageOrderEmailService.sendEmailWhenOrderIsServed(authorisation, caseData, caseDataUpdated);
            }

            // Check for Automated Hearing Management
            if (Yes.equals(caseData.getManageOrders().getCheckForAutomatedHearing())) {
                AutomatedHearingUtils.automatedHearingManagementRequest(
                    authorisation,
                    caseData,
                    caseDataUpdated,
                    manageOrderService
                );
            }

            //SNI-4330 fix
            //update caseSummaryTab with latest state
            ManageOrderService.cleanUpServeOrderOptions(caseDataUpdated);
            allTabService.submitAllTabsUpdate(
                    startAllTabsUpdateDataContent.authorisation(),
                    String.valueOf(callbackRequest.getCaseDetails().getId()),
                    startAllTabsUpdateDataContent.startEventResponse(),
                    startAllTabsUpdateDataContent.eventRequestData(),
                    caseDataUpdated);

            // Note: Custom orders are now sealed directly during combining (above), not here
            // This avoids issues with CDAM association timing

            // Clean up custom order fields to prevent them affecting subsequent order creations
            if (isCustomOrder) {
                cleanupCustomOrderFields(caseDataUpdated);
            }

            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/manage-orders/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse saveOrderDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            manageOrderService.resetChildOptions(callbackRequest);
            CaseDetails caseDetails = callbackRequest.getCaseDetails();
            CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
            caseData = manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData);
            Map<String, Object> caseDataUpdated = caseDetails.getData();

            setIsWithdrawnRequestSent(caseData, caseDataUpdated);
            try {
                setHearingData(caseData, caseDataUpdated, authorisation);
            } catch (Exception e) {
                String msg = (e.getMessage() == null || e.getMessage().isBlank())
                    ? "An error occurred while processing the order. Please check the template placeholders and try again."
                    : e.getMessage();
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseDataUpdated)
                    .errors(List.of(msg))
                    .build();
            }
            manageOrderService.setMarkedToServeEmailNotification(caseData, caseDataUpdated);
            //PRL-4216 - save server order additional documents if any
            manageOrderService.saveAdditionalOrderDocuments(authorisation, caseData, caseDataUpdated);
            //Added below fields for WA purpose
            //Add additional logged-in user check & empty check, to avoid null pointer & class cast exception, it needs refactoring in future
            //Refactoring should be done for each journey in manage order ie upload order along with the users ie court admin
            UUID newDraftOrderCollectionId = getDraftOrderId(authorisation, caseData, caseDataUpdated);
            caseDataUpdated.putAll(manageOrderService.setFieldsForWaTask(authorisation,
                                                                         caseData,
                                                                         callbackRequest.getEventId(),
                                                                         newDraftOrderCollectionId));
            CaseUtils.setCaseState(callbackRequest, caseDataUpdated);
            checkNameOfJudgeToReviewOrder(caseData, authorisation, callbackRequest);
            //Populate need to check automated hearing request
            manageOrderService.populateCheckForAutomatedRequest(caseData, caseDataUpdated, callbackRequest.getEventId());

            cleanUpSelectedManageOrderOptions(caseDataUpdated);

            cafcassDateTimeService.updateCafcassDateTime(callbackRequest);

            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new InvalidClientException(INVALID_CLIENT));
        }
    }

    private UUID getDraftOrderId(String authorisation, CaseData caseData, Map<String, Object> caseDataUpdated) {
        UUID newDraftOrderCollectionId = null;
        String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);
        if (UserRoles.COURT_ADMIN.name().equals(loggedInUserType)
            && !caseData.getManageOrdersOptions().equals(servedSavedOrders)
            && !AmendOrderCheckEnum.noCheck.equals(caseData.getManageOrders().getAmendOrderSelectCheckOptions())
            && caseDataUpdated.containsKey(DRAFT_ORDER_COLLECTION)
            && null != caseDataUpdated.get(DRAFT_ORDER_COLLECTION)) {
            List<Element<DraftOrder>> draftOrderCollection = (List<Element<DraftOrder>>) caseDataUpdated.get(
                DRAFT_ORDER_COLLECTION);

            newDraftOrderCollectionId = CollectionUtils.isNotEmpty(draftOrderCollection)
                ? draftOrderCollection.get(0).getId() : null;
        }
        return newDraftOrderCollectionId;
    }

    /*
     *  setHearingData is a misnomer this does setHearingData but then goes on to other action based on selection
     *  including serving an order
     * @param caseData original
     * @param caseDataUpdated updated
     * @param authorisation need auth for changes
     */
    private void setHearingData(CaseData caseData, Map<String, Object> caseDataUpdated, String authorisation)
        throws IOException {
        // Check if order was already added in mid-event whenToServeOrder (when serving immediately)
        // If doYouWantToServeOrder=Yes, order was already added - don't duplicate
        boolean orderAlreadyAddedInMidEvent = caseData.getServeOrderData() != null
            && Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder());

        if (caseData.getManageOrdersOptions().equals(amendOrderUnderSlipRule)) {
            caseDataUpdated.putAll(amendOrderService.updateOrder(caseData, authorisation));
        } else if (caseData.getManageOrdersOptions().equals(createCustomOrder)) {
            // Custom order flow: add order to collection using CUSTOM_ORDER_DOC
            // The submitted callback will later replace it with the combined header + content (sealed for non-draft)
            // Only add if order wasn't already added in mid-event (when serving immediately)
            if (!orderAlreadyAddedInMidEvent) {
                log.info("Custom order flow: adding order to collection with CUSTOM_ORDER_DOC");
                caseDataUpdated.putAll(manageOrderService.addOrderDetailsAndReturnReverseSortedList(
                    authorisation,
                    caseData,
                    PrlAppsConstants.ENGLISH
                ));
            } else {
                log.info("Custom order flow: order already added in mid-event (serve flow), skipping duplicate add");
            }
        } else if (caseData.getManageOrdersOptions().equals(createAnOrder)
            || caseData.getManageOrdersOptions().equals(uploadAnOrder)) {
            Hearings hearings = hearingService.getHearings(authorisation, String.valueOf(caseData.getId()));
            if (caseData.getManageOrdersOptions().equals(createAnOrder)
                && isHearingPageNeeded(
                caseData.getCreateSelectOrderOptions(),
                caseData.getManageOrders().getC21OrderOptions()
            )) {
                caseData.getManageOrders().setOrdersHearingDetails(hearingDataService
                    .getHearingDataForSelectedHearing(
                        caseData,
                        hearings,
                        authorisation
                    ));
            } else if (caseData.getManageOrdersOptions().equals(createAnOrder)
                && CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(caseData.getCreateSelectOrderOptions())) {
                caseData = manageOrderService.setHearingDataForSdo(caseData, hearings, authorisation);
            }
            // Only add if order wasn't already added in mid-event (when serving immediately)
            if (!orderAlreadyAddedInMidEvent) {
                caseDataUpdated.putAll(manageOrderService.addOrderDetailsAndReturnReverseSortedList(
                    authorisation,
                    caseData,
                    PrlAppsConstants.ENGLISH
                ));
            } else {
                log.info("Order already added in mid-event (serve flow), skipping duplicate add");
            }
        } else if (caseData.getManageOrdersOptions().equals(servedSavedOrders)) {
            caseDataUpdated.put(
                ORDER_COLLECTION,
                manageOrderService.serveOrder(caseData, caseData.getOrderCollection())
            );
        }
    }

    private void checkNameOfJudgeToReviewOrder(CaseData caseData, String authorisation, CallbackRequest callbackRequest) {
        if (null != caseData.getManageOrders().getAmendOrderSelectCheckOptions()
            && caseData.getManageOrders().getAmendOrderSelectCheckOptions()
            .equals(AmendOrderCheckEnum.judgeOrLegalAdvisorCheck)) {
            RoleAssignmentDto roleAssignmentDto = RoleAssignmentDto.builder()
                .judicialUser(caseData.getManageOrders().getAmendOrderSelectJudgeOrLa()
                                  .equals(JudgeOrLegalAdvisorCheckEnum.judge)
                                  ? caseData.getManageOrders().getNameOfJudgeToReviewOrder() : null)
                .legalAdviserList(caseData.getManageOrders().getAmendOrderSelectJudgeOrLa()
                                      .equals(JudgeOrLegalAdvisorCheckEnum.legalAdvisor)
                                      ? caseData.getManageOrders().getNameOfLaToReviewOrder() : null)
                .build();

            roleAssignmentService.createRoleAssignment(
                authorisation,
                callbackRequest.getCaseDetails(),
                roleAssignmentDto,
                Event.MANAGE_ORDERS.getName(),
                false,
                HEARING_JUDGE_ROLE
            );
        }

    }

    private static void setIsWithdrawnRequestSent(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if ((No).equals(caseData.getManageOrders().getIsCaseWithdrawn())) {
            caseDataUpdated.put("isWithdrawRequestSent", "DisApproved");
        } else {
            caseDataUpdated.put("isWithdrawRequestSent", "Approved");
        }
    }

    @PostMapping(path = "/show-preview-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to show preview order for special guardianship create order")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse showPreviewOrderWhenOrderCreated(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            if (caseData.getCreateSelectOrderOptions() != null
                && CreateSelectOrderOptionsEnum.specialGuardianShip.equals(caseData.getCreateSelectOrderOptions())) {
                List<Element<AppointedGuardianFullName>> namesList = new ArrayList<>();
                manageOrderService.updateCaseDataWithAppointedGuardianNames(
                    callbackRequest.getCaseDetails(),
                    namesList
                );
                caseData.setAppointedGuardianName(namesList);

                //PRL-4212 - update only if existing hearings are present
                List<Element<HearingData>> hearingData = manageOrderService
                    .getHearingDataFromExistingHearingData(authorisation,
                                                           caseData.getManageOrders().getOrdersHearingDetails(),
                                                           caseData);
                if (isNotEmpty(hearingData)) {
                    caseDataUpdated.put(ORDER_HEARING_DETAILS, hearingData);
                }

                caseDataUpdated.putAll(manageOrderService.getCaseData(
                    authorisation,
                    caseData,
                    caseData.getCreateSelectOrderOptions()
                ));
            }
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/amend-order/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event and set cafcassCymruEmail if the case is assigned to Welsh court")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse populateOrderToAmendDownloadLink(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

            if (caseData.getManageOrdersOptions().equals(amendOrderUnderSlipRule)) {
                caseDataUpdated.putAll(manageOrderService.getOrderToAmendDownloadLink(caseData));
            }

            caseDataUpdated.put("loggedInUserType", manageOrderService.getLoggedInUserType(authorisation));
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new InvalidClientException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/manage-orders/when-to-serve/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> whenToServeOrder(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestHeader(value = CLIENT_CONTEXT_HEADER_PARAMETER, required = false) String clientContext,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            caseData = manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData);
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            if (Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())) {
                caseDataUpdated.put(ORDERS_NEED_TO_BE_SERVED, Yes);
                if (amendOrderUnderSlipRule.equals(caseData.getManageOrdersOptions())) {
                    caseDataUpdated.putAll(amendOrderService.updateOrder(caseData, authorisation));
                } else {
                    caseDataUpdated.putAll(manageOrderService.addOrderDetailsAndReturnReverseSortedList(
                        authorisation,
                        caseData,
                        PrlAppsConstants.ENGLISH
                    ));
                }
                CaseData modifiedCaseData = objectMapper.convertValue(
                    caseDataUpdated,
                    CaseData.class
                );
                manageOrderService.populateServeOrderDetails(modifiedCaseData, caseDataUpdated);
            } else {
                caseDataUpdated.put(ORDERS_NEED_TO_BE_SERVED, No);
            }

            ResponseEntity.BodyBuilder responseBuilder =  ResponseEntity.status(HttpStatus.OK);
            if (objectMapper.convertValue(
                    caseDataUpdated.get(IS_INVOKED_FROM_TASK),
                    new TypeReference<YesOrNo>() {
                    }
                )
                .equals(Yes)) {
                String encodedClientContext = taskUtils.setTaskCompletion(
                    clientContext,
                    caseData,
                    data ->
                        !manageOrderService.isSaveAsDraft(data)
                            && ofNullable(data.getManageOrders().getOrdersHearingDetails())
                            .map(ElementUtils::unwrapElements)
                            .map(hearingData -> hearingData.getFirst().getHearingDateConfirmOptionEnum())
                            .filter(hearingDateConfirmOptionEnum ->
                                        HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab.getId()
                                            .equals(hearingDateConfirmOptionEnum.getId())).isPresent()
                );

                responseBuilder = ofNullable(encodedClientContext)
                    .map(value -> ResponseEntity.ok()
                        .header(CLIENT_CONTEXT_HEADER_PARAMETER, value))
                    .orElseGet(ResponseEntity::ok);
            }

            return responseBuilder.body(AboutToStartOrSubmitCallbackResponse.builder()
                                            .data(caseDataUpdated)
                                            .build());
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/manage-order/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse manageOrderMidEvent(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            if (caseData.getManageOrdersOptions().equals(amendOrderUnderSlipRule)) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(List.of(THIS_FEATURE_IS_NOT_CURRENTLY_AVAILABLE_PLEASE_REFER_TO_HMCTS_GUIDANCE)).build();
            }
            Map<String, Object> caseDataUpdated = new HashMap<>();
            if (caseData.getManageOrdersOptions().equals(servedSavedOrders)) {
                caseDataUpdated.put(ORDERS_NEED_TO_BE_SERVED, Yes);
            }
            //PRL-4212 - populate fields only when it's needed
            caseDataUpdated.putAll(manageOrderService.populateHeader(caseData));

            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/manage-orders/serve-order/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse serveOrderMidEvent(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse.builder().data(manageOrderService.serveOrderMidEvent(
                callbackRequest)).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/manage-orders/pre-populate-judge-or-la/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> prePopulateJudgeOrLegalAdviser(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestHeader(value = CLIENT_CONTEXT_HEADER_PARAMETER, required = false) String clientContext,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            List<DynamicListElement> legalAdviserList = refDataUserService.getLegalAdvisorList();
            caseDataUpdated.put(
                "nameOfLaToReviewOrder",
                DynamicList.builder().value(DynamicListElement.EMPTY).listItems(legalAdviserList)
                    .build()
            );

            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(HttpStatus.OK);
            if (objectMapper.convertValue(
                    caseDataUpdated.get(IS_INVOKED_FROM_TASK),
                    new TypeReference<YesOrNo>() {
                    }
                )
                .equals(Yes)) {
                String encodedClientContext = taskUtils.setTaskCompletion(
                    clientContext,
                    caseData,
                    data ->
                        ofNullable(data.getManageOrders().getOrdersHearingDetails())
                            .map(ElementUtils::unwrapElements)
                            .map(hearingData -> hearingData.getFirst().getHearingDateConfirmOptionEnum())
                            .filter(hearingDateConfirmOptionEnum ->
                                        HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab.getId()
                                            .equals(hearingDateConfirmOptionEnum.getId())).isPresent()
                );

                responseBuilder = ofNullable(encodedClientContext)
                    .map(value -> ResponseEntity.ok()
                        .header(CLIENT_CONTEXT_HEADER_PARAMETER, value))
                    .orElseGet(ResponseEntity::ok);
            }
            return responseBuilder.body(AboutToStartOrSubmitCallbackResponse.builder()
                                            .data(caseDataUpdated)
                                            .build());
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/manage-orders/validate-populate-hearing-data", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to show preview order in next screen for upload order")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse validateAndPopulateHearingData(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            List<String> errorList = new ArrayList<>();
            String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);

            if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(caseData.getCreateSelectOrderOptions())) {
                //SDO - hearing screen validations
                errorList = getHearingScreenValidationsForSdo(caseData.getStandardDirectionOrder(), PrlAppsConstants.ENGLISH);
            } else if (ManageOrdersUtils.isHearingPageNeeded(caseData.getCreateSelectOrderOptions(),
                                                             caseData.getManageOrders().getC21OrderOptions())) {
                //PRL-4260 - hearing screen validations
                errorList = getHearingScreenValidations(caseData.getManageOrders().getOrdersHearingDetails(),
                                                        caseData.getCreateSelectOrderOptions(),
                                                        false,
                                                        PrlAppsConstants.ENGLISH,
                                                        loggedInUserType
                );
            }

            if (isNotEmpty(errorList)) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errorList)
                    .build();
            }

            // For custom orders, render header preview with hearing data
            if (createCustomOrder.equals(caseData.getManageOrdersOptions())) {
                return renderCustomOrderPreviewWithHearingData(authorisation, callbackRequest, caseData);
            }

            //handle preview order
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(manageOrderService.handlePreviewOrder(callbackRequest, authorisation, PrlAppsConstants.ENGLISH))
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/manage-orders/recipients-validations",
        consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to validate respondent Lip and other person address")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse validateRespondentAndOtherPersonAddress(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            if (C100_CASE_TYPE.equals(callbackRequest.getCaseDetails().getData().get(CASE_TYPE_OF_APPLICATION))) {
                return manageOrderService.validateRespondentLipAndOtherPersonAddress(callbackRequest);
            }
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(callbackRequest.getCaseDetails().getData()).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private AboutToStartOrSubmitCallbackResponse renderCustomOrderPreviewWithHearingData(
        String authorisation,
        CallbackRequest callbackRequest,
        CaseData caseData
    ) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        try {
            String courtNameValue = customOrderService.resolveCourtName(caseData, caseDataUpdated);
            if (courtNameValue != null && !courtNameValue.isEmpty()) {
                caseData.setCourtName(courtNameValue);
            }

            uk.gov.hmcts.reform.prl.models.documents.Document previewDoc =
                customOrderService.renderAndUploadHeaderPreview(
                    authorisation,
                    callbackRequest.getCaseDetails().getId(),
                    caseData,
                    caseDataUpdated
                );
            caseDataUpdated.put(PREVIEW_ORDER_DOC, previewDoc);
            log.info("Custom order header preview rendered with hearing data: {}", previewDoc.getDocumentFileName());
        } catch (Exception e) {
            log.error("Failed to render custom order header preview with hearing data", e);
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of("Failed to render custom order preview: " + e.getMessage()))
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }

    private boolean copyCustomOrderFieldsFromCallback(Map<String, Object> callbackData, Map<String, Object> caseDataUpdated) {
        // Note: caseDataUpdated from allTabService is from the DATABASE (old data before this event).
        // The callback request data has the CURRENT data from the aboutToSubmit response (not yet persisted).
        // For custom order fields, we must use callbackRequest data.
        String[] customOrderFields = {
            CUSTOM_ORDER_DOC, PREVIEW_ORDER_DOC, "customOrderNameOption",
            "nameOfOrder", "amendOrderSelectCheckOptions", "whatDoWithOrder", "doYouWantToServeOrder"
        };
        for (String field : customOrderFields) {
            Object value = callbackData.get(field);
            if (value != null) {
                caseDataUpdated.put(field, value);
            }
        }
        return callbackData.get(CUSTOM_ORDER_DOC) != null;
    }

    private void processCustomOrder(String authorisation, CaseData caseData, Map<String, Object> caseDataUpdated) {
        // Determine if this is a draft order based on user type and settings
        String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);
        Object amendCheckObj = caseDataUpdated.get("amendOrderSelectCheckOptions");
        AmendOrderCheckEnum amendCheck = amendCheckObj != null
            ? objectMapper.convertValue(amendCheckObj, AmendOrderCheckEnum.class)
            : (caseData.getManageOrders() != null ? caseData.getManageOrders().getAmendOrderSelectCheckOptions() : null);
        boolean isDraftOrder = UserRoles.JUDGE.name().equals(loggedInUserType)
            || (UserRoles.COURT_ADMIN.name().equals(loggedInUserType)
                && (!AmendOrderCheckEnum.noCheck.equals(amendCheck)
                    || manageOrderService.isSaveAsDraft(caseData)));
        customOrderService.combineAndFinalizeCustomOrder(authorisation, caseData, caseDataUpdated, isDraftOrder);
    }

    private void cleanupCustomOrderFields(Map<String, Object> caseDataUpdated) {
        caseDataUpdated.remove(CUSTOM_ORDER_DOC);
        caseDataUpdated.remove(PREVIEW_ORDER_DOC);
        caseDataUpdated.remove("customOrderNameOption");
        caseDataUpdated.remove("nameOfOrder");
        caseDataUpdated.remove("amendOrderSelectCheckOptions");
        caseDataUpdated.remove("whatDoWithOrder");
        caseDataUpdated.remove("doYouWantToServeOrder");
        log.info("Cleaned up custom order fields after processing");
    }
}
