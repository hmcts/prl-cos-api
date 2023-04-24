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
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.AmendOrderService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.amendOrderUnderSlipRule;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.createAnOrder;
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

    @Autowired
    RefDataUserService refDataUserService;

    @Autowired
    private HearingDataService hearingDataService;

    private DynamicList retrievedHearingTypes;

    private DynamicList retrievedHearingDates;

    private DynamicList retrievedHearingChannels;

    private DynamicList retrievedHearingSubChannels;

    public static final String ORDERS_NEED_TO_BE_SERVED = "ordersNeedToBeServed";

    @PostMapping(path = "/populate-preview-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to show preview order in next screen for upload order")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse populatePreviewOrderWhenOrderUploaded(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        String caseReferenceNumber = String.valueOf(callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<Element<HearingData>> existingOrderHearingDetails = caseData.getManageOrders().getOrdersHearingDetails();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            hearingDataService.populateHearingDynamicLists(authorisation, caseReferenceNumber, caseData);
        if (caseData.getManageOrders().getOrdersHearingDetails() != null) {
            caseDataUpdated.put(
                ORDER_HEARING_DETAILS,
                hearingDataService.getHearingData(existingOrderHearingDetails,
                                                  hearingDataPrePopulatedDynamicLists, caseData
                )
            );
        }
        caseDataUpdated.putAll(manageOrderService.populatePreviewOrder(
            authorisation,
            callbackRequest,
            caseData
        ));
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    //todo: API not required
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
        log.info("C21 order options in callback:: {}", (null != caseData.getManageOrders())
            ? caseData.getManageOrders().getC21OrderOptions() : null);
        caseData = caseData.toBuilder()
            .selectedC21Order((null != caseData.getManageOrders()
                                  && caseData.getManageOrdersOptions() == ManageOrdersOptionsEnum.createAnOrder)
                                  ? caseData.getCreateSelectOrderOptions().getDisplayedValue() : " ")
            .build();
        if (callbackRequest
            .getCaseDetailsBefore() != null && callbackRequest
            .getCaseDetailsBefore().getData().get(COURT_NAME) != null) {
            caseData.setCourtName(callbackRequest
                                      .getCaseDetailsBefore().getData().get(COURT_NAME).toString());
        }
        C21OrderOptionsEnum c21OrderType = (null != caseData.getManageOrders())
            ? caseData.getManageOrders().getC21OrderOptions() : null;
        caseData = manageOrderService.getUpdatedCaseData(caseData);
        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            && !caseData.getManageOrdersOptions().equals(uploadAnOrder)) {
            caseData = manageOrderService.populateCustomOrderFields(caseData);
        }

        ManageOrders manageOrders = caseData.getManageOrders().toBuilder()
            .c21OrderOptions(c21OrderType)
            .childOption(DynamicMultiSelectList.builder()
                             .listItems(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).build())
            .loggedInUserType(manageOrderService.getLoggedInUserType(authorisation))
            .build();
        if (null != caseData.getCreateSelectOrderOptions()
            && CreateSelectOrderOptionsEnum.blankOrderOrDirections.equals(caseData.getCreateSelectOrderOptions())) {
            log.info("C21 Order:: *****{}******", manageOrders.getC21OrderOptions());
            manageOrders = manageOrders.toBuilder()
                                  .typeOfC21Order(null != manageOrders.getC21OrderOptions()
                                                      ? manageOrders.getC21OrderOptions().getDisplayedValue() : null)
                                  .build();
        }
        caseData = caseData.toBuilder()
            .manageOrders(manageOrders)
            .build();
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
        @RequestBody CallbackRequest callbackRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation
    ) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        String caseReferenceNumber = String.valueOf(callbackRequest.getCaseDetails().getId());
        log.info("Inside Prepopulate prePopulateHearingPageData for the case id {}", caseReferenceNumber);
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            hearingDataService.populateHearingDynamicLists(authorisation, caseReferenceNumber, caseData);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(
            ORDER_HEARING_DETAILS,
            ElementUtils.wrapElements(
                hearingDataService.generateHearingData(
                    hearingDataPrePopulatedDynamicLists, caseData))
        );
        caseDataUpdated.putAll(manageOrderService.populateHeader(caseData));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
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
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        Map<String, Object> caseDataUpdated = caseDetails.getData();
        if ((YesOrNo.No).equals(caseData.getManageOrders().getIsCaseWithdrawn())) {
            caseDataUpdated.put("isWithdrawRequestSent", "DisApproved");
        } else {
            caseDataUpdated.put("isWithdrawRequestSent", "Approved");
        }
        if (caseData.getManageOrdersOptions().equals(amendOrderUnderSlipRule)) {
            caseDataUpdated.putAll(amendOrderService.updateOrder(caseData, authorisation));
        } else if (caseData.getManageOrdersOptions().equals(createAnOrder)
            || caseData.getManageOrdersOptions().equals(uploadAnOrder)
            || caseData.getManageOrdersOptions().equals(servedSavedOrders)) {
            caseDataUpdated.putAll(manageOrderService.addOrderDetailsAndReturnReverseSortedList(
                authorisation,
                caseData
            ));
        }
        manageOrderService.cleanUpSelectedManageOrderOptions(caseDataUpdated);

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
        List<Element<HearingData>> existingOrderHearingDetails = caseData.getManageOrders().getOrdersHearingDetails();
        String caseReferenceNumber = String.valueOf(callbackRequest.getCaseDetails().getId());
        if (caseData.getCreateSelectOrderOptions() != null
            && CreateSelectOrderOptionsEnum.specialGuardianShip.equals(caseData.getCreateSelectOrderOptions())) {
            List<Element<AppointedGuardianFullName>> namesList = new ArrayList<>();
            manageOrderService.updateCaseDataWithAppointedGuardianNames(callbackRequest.getCaseDetails(), namesList);
            HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
                hearingDataService.populateHearingDynamicLists(authorisation, caseReferenceNumber, caseData);
            caseData.setAppointedGuardianName(namesList);
            if (caseData.getManageOrders().getOrdersHearingDetails() != null) {
                caseDataUpdated.put(ORDER_HEARING_DETAILS, hearingDataService
                    .getHearingData(existingOrderHearingDetails,
                                    hearingDataPrePopulatedDynamicLists, caseData
                    ));
            }
            caseDataUpdated.putAll(manageOrderService.getCaseData(
                authorisation,
                caseData,
                caseData.getCreateSelectOrderOptions()
            ));
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/amend-order/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse populateOrderToAmendDownloadLink(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        if (caseData.getManageOrdersOptions().equals(amendOrderUnderSlipRule)) {
            caseDataUpdated.putAll(manageOrderService.getOrderToAmendDownloadLink(caseData));
        }

        caseDataUpdated.put("loggedInUserType", manageOrderService.getLoggedInUserType(authorisation));

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
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (caseData.getServeOrderData().getDoYouWantToServeOrder().equals(YesOrNo.Yes)) {
            caseDataUpdated.put("ordersNeedToBeServed", YesOrNo.Yes);
            if (amendOrderUnderSlipRule.equals(caseData.getManageOrdersOptions())) {
                caseDataUpdated.putAll(amendOrderService.updateOrder(caseData, authorisation));
            } else {
                caseDataUpdated.putAll(manageOrderService.addOrderDetailsAndReturnReverseSortedList(
                    authorisation,
                    caseData
                ));
            }
            CaseData modifiedCaseData = objectMapper.convertValue(
                caseDataUpdated,
                CaseData.class
            );
            manageOrderService.populateServeOrderDetails(modifiedCaseData, caseDataUpdated);
        } else {
            caseDataUpdated.put("ordersNeedToBeServed", YesOrNo.No);
        }
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
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (caseData.getManageOrdersOptions().equals(servedSavedOrders)) {
            caseDataUpdated.put(ORDERS_NEED_TO_BE_SERVED, YesOrNo.Yes);
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/manage-orders/serve-order/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse serveOrderMidEvent(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        return AboutToStartOrSubmitCallbackResponse.builder().data(manageOrderService.checkOnlyC47aOrderSelectedToServe(
            callbackRequest)).build();
    }

    @PostMapping(path = "/manage-orders/pre-populate-judge-or-la/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse prePopulateJudgeOrLegalAdviser(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        List<DynamicListElement> legalAdviserList = refDataUserService.getLegalAdvisorList();
        caseDataUpdated.put("nameOfLaAmendOrder", DynamicList.builder().value(DynamicListElement.EMPTY).listItems(legalAdviserList)
            .build());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


    public static void resetChildOptions(CaseDetails caseDetails) {
        if (caseDetails.getData().containsKey(IS_THE_ORDER_ABOUT_ALL_CHILDREN) && caseDetails.getData().get(
            IS_THE_ORDER_ABOUT_ALL_CHILDREN) != null && !caseDetails.getData().get(
            IS_THE_ORDER_ABOUT_ALL_CHILDREN).toString().equalsIgnoreCase(PrlAppsConstants.NO)) {
            caseDetails.getData().put(CHILD_OPTION, DynamicMultiSelectList.builder()
                .listItems(List.of(DynamicMultiselectListElement.EMPTY))
                .value(List.of(DynamicMultiselectListElement.EMPTY))
                .build());
        }
        if (caseDetails.getData().containsKey(IS_THE_ORDER_ABOUT_CHILDREN) && caseDetails.getData().get(
            IS_THE_ORDER_ABOUT_CHILDREN) != null && caseDetails.getData().get(
            IS_THE_ORDER_ABOUT_CHILDREN).toString().equalsIgnoreCase(PrlAppsConstants.NO)) {
            caseDetails.getData().put(CHILD_OPTION, DynamicMultiSelectList.builder()
                .listItems(List.of(DynamicMultiselectListElement.EMPTY))
                    .value(List.of(DynamicMultiselectListElement.EMPTY))
                .build());
        }
    }
}
