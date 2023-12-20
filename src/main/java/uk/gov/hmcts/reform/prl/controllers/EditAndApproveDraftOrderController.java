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
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_ORDER_NAME_JUDGE_APPROVED;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EditAndApproveDraftOrderController {
    public static final String WHAT_TO_DO_WITH_ORDER_SOLICITOR = "whatToDoWithOrderSolicitor";
    private final ObjectMapper objectMapper;
    private final DraftAnOrderService draftAnOrderService;
    private final ManageOrderService manageOrderService;
    private final ManageOrderEmailService manageOrderEmailService;
    private final AuthorisationService authorisationService;
    private final CoreCaseDataService coreCaseDataService;

    public static final String CONFIRMATION_HEADER = "# Order approved";
    public static final String CONFIRMATION_BODY_FURTHER_DIRECTIONS = """
        ### What happens next \n We will send this order to admin.
        \n\n If you have included further directions, admin will also receive them.
        """;
    public static final String CONFIRMATION_HEADER_LEGAL_REP = "# Message sent to legal representative";
    public static final String CONFIRMATION_BODY_FURTHER_DIRECTIONS_LEGAL_REP = """
        ### What happens next \n Your message has been sent to the legal representative.
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
                    .data(draftAnOrderService.getDraftOrderDynamicList(caseData, callbackRequest.getEventId())).build();
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
                    caseData)).build();
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
            manageOrderService.resetChildOptions(callbackRequest);
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            log.info("*** draft order dynamic list: {}", caseDataUpdated.get("draftOrdersDynamicList"));
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            log.info("*** draft order dynamic list: {}", caseData.getDraftOrdersDynamicList());
            caseData = manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData);
            if (Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId()
                .equalsIgnoreCase(callbackRequest.getEventId())) {
                caseDataUpdated.putAll(draftAnOrderService.adminEditAndServeAboutToSubmit(
                    authorisation,
                    callbackRequest
                ));
            } else if (Event.EDIT_AND_APPROVE_ORDER.getId()
                .equalsIgnoreCase(callbackRequest.getEventId())) {
                caseDataUpdated.put(WA_ORDER_NAME_JUDGE_APPROVED, draftAnOrderService.getDraftOrderNameForWA(caseData, true));
                caseDataUpdated.putAll(draftAnOrderService.updateDraftOrderCollection(
                    caseData,
                    authorisation,
                    callbackRequest.getEventId()
                ));
            }
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
            DraftOrder selectedOrder = draftAnOrderService.getSelectedDraftOrderDetails(caseData);
            if (selectedOrder != null && (CreateSelectOrderOptionsEnum.blankOrderOrDirections.equals(selectedOrder.getOrderType()))
            ) {
                caseData = draftAnOrderService.updateCustomFieldsWithApplicantRespondentDetails(callbackRequest, caseData);
                caseDataUpdated.putAll(draftAnOrderService.getDraftOrderInfo(authorisation, caseData));
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseDataUpdated).build();
            }
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(draftAnOrderService.populateDraftOrderCustomFields(caseData, authorisation)).build();
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
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            log.info("*** draft order dynamic list: {}", callbackRequest.getCaseDetails().getData().get("draftOrdersDynamicList"));
            Map<String, Object> response = draftAnOrderService.populateCommonDraftOrderFields(authorisation, caseData);
            boolean isOrderEdited = false;
            isOrderEdited = draftAnOrderService.isOrderEdited(caseData, callbackRequest.getEventId(), isOrderEdited);
            if (isOrderEdited) {
                response.put("doYouWantToEditTheOrder", caseData.getDoYouWantToEditTheOrder());
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
            if (DraftAnOrderService.checkStandingOrderOptionsSelected(caseData)) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(draftAnOrderService.populateStandardDirectionOrder(authorisation, caseData, true)).build();
            } else {
                List<String> errorList = new ArrayList<>();
                errorList.add(
                    "Please select at least one options from below");
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
            ManageOrderService.cleanUpServeOrderOptions(caseDataUpdated);
            caseDataUpdated.put(STATE, caseData.getState());
            coreCaseDataService.triggerEvent(
                JURISDICTION,
                CASE_TYPE,
                caseData.getId(),
                "internal-update-all-tabs",
                caseDataUpdated
            );
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
            log.info("Solicitor created order options {}",caseDataUpdated.get(WHAT_TO_DO_WITH_ORDER_SOLICITOR));
            log.info("Court admin created order options {}",caseDataUpdated.get("whatToDoWithOrderCourtAdmin"));
            ResponseEntity<SubmittedCallbackResponse> responseEntity = ResponseEntity
                .ok(SubmittedCallbackResponse.builder()
                        .confirmationHeader(CONFIRMATION_HEADER)
                        .confirmationBody(CONFIRMATION_BODY_FURTHER_DIRECTIONS).build());
            if (JudgeApprovalDecisionsSolicitorEnum.askLegalRepToMakeChanges.toString()
                .equalsIgnoreCase(String.valueOf(caseDataUpdated.get(WHAT_TO_DO_WITH_ORDER_SOLICITOR)))) {
                CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
                log.info("*** Draft order dynamic list : {}", caseData.getDraftOrdersDynamicList());
                log.info("*** Draft order collection : {}", caseData.getDraftOrderCollection());
                try {
                    DraftOrder draftOrder = draftAnOrderService.getSelectedDraftOrderDetails(caseData);
                    manageOrderEmailService.sendEmailToLegalRepresentativeOnRejection(callbackRequest.getCaseDetails(), draftOrder);
                } catch (Exception e) {
                    log.error("Failed to send email to solicitor : {}", e.getMessage());
                }
                responseEntity = ResponseEntity.ok(SubmittedCallbackResponse.builder()
                                             .confirmationHeader(CONFIRMATION_HEADER_LEGAL_REP)
                                             .confirmationBody(CONFIRMATION_BODY_FURTHER_DIRECTIONS_LEGAL_REP)
                                             .build());
            }
            ManageOrderService.cleanUpSelectedManageOrderOptions(caseDataUpdated);
            coreCaseDataService.triggerEvent(
                JURISDICTION,
                CASE_TYPE,
                (Long) caseDataUpdated.get("id"),
                "internal-update-all-tabs",
                caseDataUpdated
            );
            return responseEntity;
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
