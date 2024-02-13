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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.editandapprove.OrderApprovalDecisionsForSolicitorOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.EditReturnedOrderService;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_ORDER_NAME_JUDGE_APPROVED;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@SuppressWarnings({"squid:S5665"})
@RestController
@RequiredArgsConstructor
public class EditAndApproveDraftOrderController {
    public static final String WHAT_TO_DO_WITH_ORDER_SOLICITOR = "whatToDoWithOrderSolicitor";
    private final ObjectMapper objectMapper;
    private final DraftAnOrderService draftAnOrderService;
    private final ManageOrderService manageOrderService;
    private final ManageOrderEmailService manageOrderEmailService;
    private final AuthorisationService authorisationService;
    private final EditReturnedOrderService editReturnedOrderService;

    public static final String CONFIRMATION_HEADER = "# Order approved";
    public static final String CONFIRMATION_BODY_FURTHER_DIRECTIONS = """
        ### What happens next \n We will send this order to admin.
        \nIf you have included further directions, admin will also receive them.
        """;
    public static final String CONFIRMATION_HEADER_LEGAL_REP = "# Message sent to legal representative";
    public static final String CONFIRMATION_BODY_FURTHER_DIRECTIONS_LEGAL_REP = """
        ### What happens next \nYour message has been sent to the legal representative.
        """;

    @PostMapping(path = "/populate-draft-order-dropdown", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Populate draft order dropdown")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback to populate draft order dropdown"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse generateDraftOrderDropDown(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            if (caseData.getDraftOrderCollection() != null
                && !caseData.getDraftOrderCollection().isEmpty()) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(draftAnOrderService.getDraftOrderDynamicList(caseData, callbackRequest.getEventId(), authorisation)).build();
            } else {
                return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of("There are no draft orders")).build();
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/judge-or-admin-populate-draft-order",
        consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Remove dynamic list from the caseData")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback to populate draft order dropdown"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateJudgeOrAdminDraftOrder(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(draftAnOrderService.populateDraftOrderDocument(
                    caseData, authorisation)).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }

    }

    @PostMapping(path = "/judge-or-admin-edit-approve/mid-event", consumes = APPLICATION_JSON,
        produces = APPLICATION_JSON)
    @Operation(description = "Callback to generate draft order collection")
    public AboutToStartOrSubmitCallbackResponse prepareDraftOrderCollection(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            Map<String, Object> caseDataUpdated = draftAnOrderService.getEligibleServeOrderDetails(
                authorisation,
                callbackRequest
            );
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/judge-or-admin-edit-approve/about-to-submit",
        consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Remove dynamic list from the caseData")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback to populate draft order dropdown"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse saveServeOrderDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);
            manageOrderService.resetChildOptions(callbackRequest);
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            caseData = manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData);
            if (Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId()
                .equalsIgnoreCase(callbackRequest.getEventId())) {
                caseDataUpdated.putAll(draftAnOrderService.adminEditAndServeAboutToSubmit(
                    authorisation,
                    callbackRequest
                ));
            } else if (Event.EDIT_AND_APPROVE_ORDER.getId()
                .equalsIgnoreCase(callbackRequest.getEventId())) {
                if (Event.EDIT_AND_APPROVE_ORDER.getId()
                    .equalsIgnoreCase(callbackRequest.getEventId())) {
                    caseDataUpdated.put(WA_ORDER_NAME_JUDGE_APPROVED, draftAnOrderService
                        .getDraftOrderNameForWA(caseData, true));
                }

                manageOrderService.setHearingOptionDetailsForTask(caseData, caseDataUpdated,callbackRequest.getEventId(),loggedInUserType);

                caseDataUpdated.put(WA_ORDER_NAME_JUDGE_APPROVED, draftAnOrderService.getDraftOrderNameForWA(caseData, true));
                caseDataUpdated.putAll(draftAnOrderService.updateDraftOrderCollection(
                    caseData,
                    authorisation,
                    callbackRequest.getEventId()
                ));
            } else if (Event.EDIT_RETURNED_ORDER.getId()
                .equalsIgnoreCase(callbackRequest.getEventId())) {
                caseDataUpdated.putAll(editReturnedOrderService.updateDraftOrderCollection(caseData, authorisation));
            }
            ManageOrderService.cleanUpSelectedManageOrderOptions(caseDataUpdated);
            CaseUtils.setCaseState(callbackRequest, caseDataUpdated);
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/judge-or-admin-populate-draft-order-custom-fields", consumes = APPLICATION_JSON,
        produces = APPLICATION_JSON)
    @Operation(description = "Remove dynamic list from the caseData")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback to populate draft order dropdown"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateJudgeOrAdminDraftOrderCustomFields(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws  Exception {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );

            List<String> errorList = ManageOrdersUtils.validateMandatoryJudgeOrMagistrate(caseData);
            if (isNotEmpty(errorList)) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errorList)
                    .build();
            }

            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            Object dynamicList = caseData.getDraftOrdersDynamicList();
            if (Event.EDIT_RETURNED_ORDER.getId().equals(callbackRequest.getEventId())) {
                if (Yes.getDisplayedValue().equalsIgnoreCase(String.valueOf(caseDataUpdated.get("orderUploadedAsDraftFlag")))) {
                    return AboutToStartOrSubmitCallbackResponse.builder()
                        .data(caseDataUpdated).build();
                }
                dynamicList = caseData.getManageOrders().getRejectedOrdersDynamicList();
            }
            DraftOrder selectedOrder = draftAnOrderService.getSelectedDraftOrderDetails(caseData.getDraftOrderCollection(),
                                                                                        dynamicList);
            if (selectedOrder != null && (CreateSelectOrderOptionsEnum.blankOrderOrDirections.equals(selectedOrder.getOrderType()))
            ) {
                caseData = draftAnOrderService.updateCustomFieldsWithApplicantRespondentDetails(callbackRequest, caseData);
                caseDataUpdated.putAll(draftAnOrderService.getDraftOrderInfo(authorisation, caseData, selectedOrder));
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseDataUpdated).build();
            }
            //PRL-4854 - skip default call for upload
            if (null != selectedOrder && Yes.equals(selectedOrder.getIsOrderUploadedByJudgeOrAdmin())) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseDataUpdated).build();
            }
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(draftAnOrderService.populateDraftOrderCustomFields(caseData, selectedOrder)).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }

    }

    @PostMapping(path = "/judge-or-admin-populate-draft-order-common-fields", consumes = APPLICATION_JSON,
        produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate common fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated common fields"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateCommonFields(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            Object dynamicList = caseData.getDraftOrdersDynamicList();
            if (Event.EDIT_RETURNED_ORDER.getId().equals(callbackRequest.getEventId())) {
                dynamicList = caseData.getManageOrders().getRejectedOrdersDynamicList();
            }
            DraftOrder selectedOrder = draftAnOrderService.getSelectedDraftOrderDetails(caseData.getDraftOrderCollection(), dynamicList);
            Map<String, Object> response = draftAnOrderService.populateCommonDraftOrderFields(authorisation, caseData, selectedOrder);

            if (ManageOrdersUtils.isOrderEdited(caseData, callbackRequest.getEventId())) {
                response.put("doYouWantToEditTheOrder", Yes);
            }
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(response).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/judge-or-admin-edit-approve/serve-order/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse editAndServeOrderMidEvent(
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

    @PostMapping(path = "/pre-populate-standard-direction-order-other-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate standard direction order fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateSdoOtherFields(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            List<String> errorList = new ArrayList<>();
            if (DraftAnOrderService.checkStandingOrderOptionsSelected(caseData, errorList)
                && DraftAnOrderService.validationIfDirectionForFactFindingSelected(caseData, errorList)) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(draftAnOrderService.populateStandardDirectionOrder(authorisation, caseData, true)).build();
            } else {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errorList)
                    .build();
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/edit-and-serve/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Send Email Notification on Case order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse sendEmailNotificationToRecipientsServeOrder(
        @RequestHeader(javax.ws.rs.core.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            if (Yes.equals(caseData.getManageOrders().getMarkedToServeEmailNotification())) {
                manageOrderEmailService.sendEmailWhenOrderIsServed(authorisation, caseData, caseDataUpdated);
            }
            caseDataUpdated.put(STATE, caseData.getState());
            ManageOrdersUtils.clearFieldsAfterApprovalAndServe(caseDataUpdated);
            ManageOrderService.cleanUpServeOrderOptions(caseDataUpdated);
            draftAnOrderService.updateCaseData(callbackRequest, caseDataUpdated);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/edit-and-approve/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to display confirmation and send notifications")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SubmittedCallbackResponse> handleEditAndApproveSubmitted(
        @RequestHeader("Authorization")
        @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            ResponseEntity<SubmittedCallbackResponse> responseEntity = ResponseEntity
                .ok(SubmittedCallbackResponse.builder()
                        .confirmationHeader(CONFIRMATION_HEADER)
                        .confirmationBody(CONFIRMATION_BODY_FURTHER_DIRECTIONS).build());
            if (OrderApprovalDecisionsForSolicitorOrderEnum.askLegalRepToMakeChanges.toString()
                .equalsIgnoreCase(String.valueOf(caseDataUpdated.get(WHAT_TO_DO_WITH_ORDER_SOLICITOR)))) {
                CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
                try {
                    DraftOrder draftOrder = draftAnOrderService
                        .getSelectedDraftOrderDetails(caseData.getDraftOrderCollection(), caseData.getDraftOrdersDynamicList());
                    manageOrderEmailService.sendEmailToLegalRepresentativeOnRejection(callbackRequest.getCaseDetails(), draftOrder);
                } catch (Exception e) {
                    log.error("Failed to send email to solicitor :", e);
                }
                responseEntity = ResponseEntity.ok(SubmittedCallbackResponse.builder()
                                             .confirmationHeader(CONFIRMATION_HEADER_LEGAL_REP)
                                             .confirmationBody(CONFIRMATION_BODY_FURTHER_DIRECTIONS_LEGAL_REP)
                                             .build());
            }
            ManageOrdersUtils.clearFieldsAfterApprovalAndServe(caseDataUpdated);
            draftAnOrderService.updateCaseData(callbackRequest, caseDataUpdated);
            return responseEntity;
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
