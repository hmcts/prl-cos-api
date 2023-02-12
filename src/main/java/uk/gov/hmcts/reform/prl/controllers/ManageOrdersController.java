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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.AmendOrderService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.amendOrderUnderSlipRule;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.servedSavedOrders;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.uploadAnOrder;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ManageOrdersController {

    public static final String IS_THE_ORDER_ABOUT_CHILDREN = "isTheOrderAboutChildren";
    public static final String IS_THE_ORDER_ABOUT_ALL_CHILDREN = "isTheOrderAboutAllChildren";
    public static final String CHILD_OPTION = "childOption";
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ManageOrderService manageOrderService;

    @Autowired
    private final DocumentLanguageService documentLanguageService;

    @Autowired
    private ManageOrderEmailService manageOrderEmailService;

    @Autowired
    private AmendOrderService amendOrderService;

    @Autowired
    private DynamicMultiSelectListService dynamicMultiSelectListService;

    public static final String ORDERS_NEED_TO_BE_SERVED = "ordersNeedToBeServed";
    private static final List<String> MANAGE_ORDER_FIELDS = List.of("manageOrdersOptions",
                                                                    "createSelectOrderOptions",
                                                                    "childArrangementOrders",
                                                                    "domesticAbuseOrders",
                                                                    "fcOrders",
                                                                    "otherOrdersOption",
                                                                    "amendOrderDynamicList",
                                                                    "serveOrderDynamicList",
                                                                    "ordersNeedToBeServed",
                                                                    "isJudgeOrLa"
    );

    @PostMapping(path = "/populate-preview-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to show preview order in next screen for upload order")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse populatePreviewOrderWhenOrderUploaded(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (callbackRequest
            .getCaseDetailsBefore() != null && callbackRequest
            .getCaseDetailsBefore().getData().get(COURT_NAME) != null) {
            caseData.setCourtName(callbackRequest
                                      .getCaseDetailsBefore().getData().get(COURT_NAME).toString());
        }
        if (caseData.getCreateSelectOrderOptions() != null && caseData.getDateOrderMade() != null) {
            caseDataUpdated.putAll(manageOrderService.getCaseData(authorisation, caseData, caseData.getCreateSelectOrderOptions()));
        } else {
            caseDataUpdated.put("previewOrderDoc", caseData.getAppointmentOfGuardian());
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();

    }

    @PostMapping(path = "/fetch-child-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to fetch case data and custom order fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Child details are fetched"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public CallbackResponse fetchOrderDetails(
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        caseData = manageOrderService.getUpdatedCaseData(caseData);
        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            caseData = manageOrderService.populateCustomOrderFields(caseData);
        }
        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }

    @PostMapping(path = "/fetch-order-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to fetch case data and custom order fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order details are fetched"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public CallbackResponse prepopulateFL401CaseDetails(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        log.info("fetch-order-details before caseData ===> " + callbackRequest.getCaseDetails().getData());
        if (callbackRequest
            .getCaseDetailsBefore() != null && callbackRequest
            .getCaseDetailsBefore().getData().get(COURT_NAME) != null) {
            caseData.setCourtName(callbackRequest
                                      .getCaseDetailsBefore().getData().get(COURT_NAME).toString());
        }
        caseData = manageOrderService.getUpdatedCaseData(caseData);
        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            && !caseData.getManageOrdersOptions().equals(uploadAnOrder)) {
            caseData = manageOrderService.populateCustomOrderFields(caseData);
        }
        boolean isJudgeOrLa = manageOrderService.isLoggedInAsJudgeOrLa(authorisation);

        log.info("isJudgeOrLa {}", isJudgeOrLa);

        ManageOrders manageOrders = caseData.getManageOrders().toBuilder()
            .childOption(DynamicMultiSelectList.builder()
                             .listItems(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).build())
            .isJudgeOrLa(isJudgeOrLa ? PrlAppsConstants.JUDGE : PrlAppsConstants.CASEWORKER)
            .build();
        log.info("**Manage orders with child list {}", manageOrders);

        caseData = caseData.toBuilder()
            .manageOrders(manageOrders)
            .build();
        log.info("after fetch-order-details caseData ===> " + caseData);
        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }

    @PostMapping(path = "/populate-header", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateHeader(
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        log.info("caseData before populate-header ===> " + caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(manageOrderService.populateHeader(caseData))
            .build();
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
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        manageOrderEmailService.sendEmailToCafcassAndOtherParties(caseDetails);
        manageOrderEmailService.sendEmailToApplicantAndRespondent(caseDetails);
        manageOrderEmailService.sendFinalOrderIssuedNotification(caseDetails);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/manage-orders/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse saveOrderDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        resetChildOptions(caseDetails);
        log.info("before about to submit caseDetails =====> " + caseDetails);
        CaseData caseData = CaseUtils.getCaseData(caseDetails,objectMapper);
        Map<String, Object> caseDataUpdated = caseDetails.getData();
        if ((YesOrNo.No).equals(caseData.getManageOrders().getIsCaseWithdrawn())) {
            caseDataUpdated.put("isWithdrawRequestSent", "DisApproved");
        } else {
            caseDataUpdated.put("isWithdrawRequestSent", "Approved");
        }

        if (caseData.getManageOrdersOptions().equals(amendOrderUnderSlipRule)) {
            caseDataUpdated.putAll(amendOrderService.updateOrder(caseData, authorisation));
        } else {
            caseDataUpdated.putAll(manageOrderService.addOrderDetailsAndReturnReverseSortedList(
                authorisation,
                caseData
            ));
        }
        cleanUpSelectedManageOrderOptions(caseDataUpdated);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/show-preview-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to show preview order for special guardianship create order")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse showPreviewOrderWhenOrderCreated(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (caseData.getCreateSelectOrderOptions() != null
            && CreateSelectOrderOptionsEnum.specialGuardianShip.equals(caseData.getCreateSelectOrderOptions())) {
            List<Element<AppointedGuardianFullName>> namesList = new ArrayList<>();
            manageOrderService.updateCaseDataWithAppointedGuardianNames(callbackRequest.getCaseDetails(), namesList);
            caseData.setAppointedGuardianName(namesList);
            caseDataUpdated.putAll(manageOrderService.getCaseData(authorisation, caseData, caseData.getCreateSelectOrderOptions()));
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/amend-order/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse populateOrderToAmendDownloadLink(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("/amend-order/mid-event before" + callbackRequest.getCaseDetails());
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        boolean isJudgeOrLa = manageOrderService.isLoggedInAsJudgeOrLa(authorisation);

        if (caseData.getManageOrdersOptions().equals(amendOrderUnderSlipRule)) {
            caseDataUpdated.putAll(manageOrderService.getOrderToAmendDownloadLink(caseData));
        }

        log.info("isJudgeOrLa {}", isJudgeOrLa);
        caseDataUpdated.put("isJudgeOrLa", isJudgeOrLa ? PrlAppsConstants.JUDGE : PrlAppsConstants.CASEWORKER);

        log.info("/amend-order/mid-event after" + caseDataUpdated);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/manage-orders/add-upload-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse addUploadOrder(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(),objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("/manage-orders/add-upload-order before caseData ===> " + callbackRequest.getCaseDetails());
        caseDataUpdated.putAll(manageOrderService.addOrderDetailsAndReturnReverseSortedList(
            authorisation,
            caseData
        ));
        if (caseData.getServeOrderData().getDoYouWantToServeOrder().equals(YesOrNo.Yes)) {
            caseDataUpdated.put(ORDERS_NEED_TO_BE_SERVED, YesOrNo.Yes);
        }

        CaseData modifiedCaseData = objectMapper.convertValue(
            caseDataUpdated,
            CaseData.class
        );
        log.info("modifiedCaseData ===> " + modifiedCaseData);

        caseDataUpdated.putAll(manageOrderService.populateHeader(modifiedCaseData));
        log.info("/manage-orders/add-upload-order after caseDataUpdated ===> " + caseDataUpdated);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }


    @PostMapping(path = "/manage-order/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse manageOrderMidEvent(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("/manage-order/mid-event before" + callbackRequest.getCaseDetails());
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (caseData.getManageOrdersOptions().equals(servedSavedOrders)) {
            caseDataUpdated.put(ORDERS_NEED_TO_BE_SERVED, YesOrNo.Yes);
        }

        log.info("/manage-order/mid-event after" + caseDataUpdated);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    private static void cleanUpSelectedManageOrderOptions(Map<String, Object> caseDataUpdated) {
        log.info("caseDataUpdated before cleanup ===> " + caseDataUpdated);
        for (String field : MANAGE_ORDER_FIELDS) {
            if (caseDataUpdated.containsKey(field)) {
                caseDataUpdated.remove(field);
            }
        }
        log.info("orderCollection after cleanup ===> " + caseDataUpdated.get("orderCollection"));
        log.info("draftOrderCollection after cleanup ===> " + caseDataUpdated.get("draftOrderCollection"));
    }

    private static void resetChildOptions(CaseDetails caseDetails) {
        if (caseDetails.getData().containsKey(IS_THE_ORDER_ABOUT_ALL_CHILDREN) && caseDetails.getData().get(
            IS_THE_ORDER_ABOUT_ALL_CHILDREN) != null && !caseDetails.getData().get(
            IS_THE_ORDER_ABOUT_ALL_CHILDREN).toString().equalsIgnoreCase(PrlAppsConstants.NO)) {
            log.info("inside isTheOrderAboutAllChildren =====> " + caseDetails.getData().get(CHILD_OPTION));
            caseDetails.getData().put(CHILD_OPTION, DynamicMultiSelectList.builder()
                .listItems(List.of(DynamicMultiselectListElement.EMPTY)).build());
        }
        if (caseDetails.getData().containsKey(IS_THE_ORDER_ABOUT_CHILDREN) && caseDetails.getData().get(
            IS_THE_ORDER_ABOUT_CHILDREN) != null && caseDetails.getData().get(
            IS_THE_ORDER_ABOUT_CHILDREN).toString().equalsIgnoreCase(PrlAppsConstants.NO)) {
            log.info("inside isTheOrderAboutChildren =====> " + caseDetails.getData().get(CHILD_OPTION));
            caseDetails.getData().put(CHILD_OPTION, DynamicMultiSelectList.builder()
                .listItems(List.of(DynamicMultiselectListElement.EMPTY)).build());
        }
    }
}
