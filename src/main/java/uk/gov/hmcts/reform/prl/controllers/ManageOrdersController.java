package uk.gov.hmcts.reform.prl.controllers;

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
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.AutomatedHearingUtils;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.State.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.prl.enums.State.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.amendOrderUnderSlipRule;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.createAnOrder;
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
    private final ManageOrderEmailService manageOrderEmailService;
    private final AmendOrderService amendOrderService;
    private final RefDataUserService refDataUserService;
    private final HearingDataService hearingDataService;
    private final AuthorisationService authorisationService;
    private final AllTabServiceImpl allTabService;
    private final RoleAssignmentService roleAssignmentService;
    private final HearingService hearingService;

    public static final String ORDERS_NEED_TO_BE_SERVED = "ordersNeedToBeServed";

    @PostMapping(path = "/populate-preview-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to show preview order in next screen for upload order")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse populatePreviewOrderWhenOrderUploaded(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestHeader(value = PrlAppsConstants.CLIENT_CONTEXT_HEADER_PARAMETER, required = false) String clientContext,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

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
    public AboutToStartOrSubmitCallbackResponse prepopulateFL401CaseDetails(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestHeader(value = PrlAppsConstants.CLIENT_CONTEXT_HEADER_PARAMETER, required = false) String clientContext,
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
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
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
    public AboutToStartOrSubmitCallbackResponse sendEmailNotificationOnClosingOrder(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
                    = allTabService.getStartAllTabsUpdate(String.valueOf(callbackRequest.getCaseDetails().getId()));
            Map<String, Object> caseDataUpdated = startAllTabsUpdateDataContent.caseDataMap();
            //SNI-4330 fix - this will set state in caseData
            //updating state in caseData so that caseSummaryTab is updated with latest state
            CaseData caseData = startAllTabsUpdateDataContent.caseData();
            try {
                log.info("Initiating court seal for the orders");
                manageOrderService.addSealToOrders(authorisation, caseData, caseDataUpdated);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            log.info("Notifications to be sent? - {}", caseData.getManageOrders().getMarkedToServeEmailNotification());
            if (Yes.equals(caseData.getManageOrders().getMarkedToServeEmailNotification())) {
                log.info("Preparing to send notifications to parties");
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
            log.info("Inside about-to-submit callback --------->>>>>>");
            manageOrderService.resetChildOptions(callbackRequest);
            CaseDetails caseDetails = callbackRequest.getCaseDetails();
            CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
            caseData = manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData);
            Map<String, Object> caseDataUpdated = caseDetails.getData();
            setIsWithdrawnRequestSent(caseData, caseDataUpdated);
            setHearingData(caseData, caseDataUpdated, authorisation);

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

    private void setHearingData(CaseData caseData, Map<String, Object> caseDataUpdated, String authorisation) {
        if (caseData.getManageOrdersOptions().equals(amendOrderUnderSlipRule)) {
            caseDataUpdated.putAll(amendOrderService.updateOrder(caseData, authorisation));
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
            caseDataUpdated.putAll(manageOrderService.addOrderDetailsAndReturnReverseSortedList(
                authorisation,
                caseData,
                PrlAppsConstants.ENGLISH
            ));
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
    public AboutToStartOrSubmitCallbackResponse whenToServeOrder(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            caseData = manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData);
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            if (caseData.getServeOrderData().getDoYouWantToServeOrder().equals(YesOrNo.Yes)) {
                caseDataUpdated.put(ORDERS_NEED_TO_BE_SERVED, YesOrNo.Yes);
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
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated)
                .build();
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
                caseDataUpdated.put(ORDERS_NEED_TO_BE_SERVED, YesOrNo.Yes);
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
    public AboutToStartOrSubmitCallbackResponse prePopulateJudgeOrLegalAdviser(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            List<DynamicListElement> legalAdviserList = refDataUserService.getLegalAdvisorList();
            caseDataUpdated.put(
                "nameOfLaToReviewOrder",
                DynamicList.builder().value(DynamicListElement.EMPTY).listItems(legalAdviserList)
                    .build()
            );
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
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
}
