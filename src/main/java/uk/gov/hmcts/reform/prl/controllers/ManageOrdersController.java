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
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.AmendOrderService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE;
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

@Slf4j
@RestController
@RequiredArgsConstructor
public class ManageOrdersController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ManageOrderService manageOrderService;

    @Autowired
    private ManageOrderEmailService manageOrderEmailService;

    @Autowired
    private AmendOrderService amendOrderService;

    @Autowired
    RefDataUserService refDataUserService;

    @Autowired
    private HearingDataService hearingDataService;

    @Autowired
    private AuthorisationService authorisationService;

    @Autowired
    CoreCaseDataService coreCaseDataService;

    private final DynamicMultiSelectListService dynamicMultiSelectListService;

    private final HearingService hearingService;

    public static final String ORDERS_NEED_TO_BE_SERVED = "ordersNeedToBeServed";

    @PostMapping(path = "/populate-preview-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to show preview order in next screen for upload order")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse populatePreviewOrderWhenOrderUploaded(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

            List<String> errorList = ManageOrdersUtils.validateMandatoryJudgeOrMagistrate(caseData);
            errorList.addAll(getErrorForOccupationScreen(caseData, caseData.getCreateSelectOrderOptions()));
            if (isNotEmpty(errorList)) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errorList)
                    .build();
            }
            return AboutToStartOrSubmitCallbackResponse.builder().data(manageOrderService.handlePreviewOrder(
                callbackRequest,
                authorisation
            )).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    //todo: API not required
    @PostMapping(path = "/fetch-child-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to fetch case data and custom order fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Child details are fetched"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public CallbackResponse fetchOrderDetails(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {

            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                caseData = manageOrderService.populateCustomOrderFields(caseData);
            }
            return CallbackResponse.builder()
                .data(caseData)
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
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
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );

            List<String> errorList = new ArrayList<>();
            if (ManageOrdersOptionsEnum.createAnOrder.equals(caseData.getManageOrdersOptions())
                && getErrorsForOrdersProhibitedForC100FL401(
                caseData,
                caseData.getCreateSelectOrderOptions(),
                errorList
            )) {
                return AboutToStartOrSubmitCallbackResponse.builder().errors(errorList).build();
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(manageOrderService.handleFetchOrderDetails(authorisation, callbackRequest))
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
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            //SNI-4330 fix - this will set state in caseData
            //updating state in caseData so that caseSummaryTab is updated with latest state
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

            if (Yes.equals(caseData.getManageOrders().getMarkedToServeEmailNotification())) {
                log.info("** Calling email service to send emails to recipients on serve order - manage orders**");
                manageOrderEmailService.sendEmailWhenOrderIsServed(authorisation, caseData, caseDataUpdated);
            }
            // The following can be removed or utilised based on requirement
            /* final CaseDetails caseDetails = callbackRequest.getCaseDetails();
            manageOrderEmailService.sendEmailToCafcassAndOtherParties(caseDetails);
            manageOrderEmailService.sendEmailToApplicantAndRespondent(caseDetails);
            manageOrderEmailService.sendFinalOrderIssuedNotification(caseDetails); */

            //SNI-4330 fix
            //update caseSummaryTab with latest state
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

    @PostMapping(path = "/manage-orders/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse saveOrderDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            String performingUser = null;
            String performingAction = null;
            String judgeLaReviewRequired = null;
            String orderNameForWA = null;
            manageOrderService.resetChildOptions(callbackRequest);
            CaseDetails caseDetails = callbackRequest.getCaseDetails();
            CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
            log.info("*** recipientsOptions {}", caseData.getManageOrders().getRecipientsOptions());
            log.info("*** ServeOrderAdditionalDocuments {}", caseData.getManageOrders().getServeOrderAdditionalDocuments());
            caseData = manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData);
            Map<String, Object> caseDataUpdated = caseDetails.getData();
            setIsWithdrawnRequestSent(caseData, caseDataUpdated);
            if (caseData.getManageOrdersOptions().equals(amendOrderUnderSlipRule)) {
                caseDataUpdated.putAll(amendOrderService.updateOrder(caseData, authorisation));
            } else if (caseData.getManageOrdersOptions().equals(createAnOrder)
                || caseData.getManageOrdersOptions().equals(uploadAnOrder)) {
                if (null != caseData.getManageOrders().getOrdersHearingDetails()) {
                    Hearings hearings = hearingService.getHearings(authorisation, String.valueOf(caseData.getId()));
                    caseData.getManageOrders().setOrdersHearingDetails(hearingDataService
                                                                           .getHearingDataForSelectedHearing(
                                                                               caseData,
                                                                               hearings
                                                                           ));
                }
                log.info("*** Court seal 0 {}", caseData.getCourtSeal());
                caseDataUpdated.putAll(manageOrderService.addOrderDetailsAndReturnReverseSortedList(
                    authorisation,
                    caseData
                ));
            } else if (caseData.getManageOrdersOptions().equals(servedSavedOrders)) {
                caseDataUpdated.put(ORDER_COLLECTION, manageOrderService.serveOrder(caseData, caseData.getOrderCollection()));
            }
            manageOrderService.setMarkedToServeEmailNotification(caseData, caseDataUpdated);
            //PRL-4216 - save server order additional documents if any
            manageOrderService.saveAdditionalOrderDocuments(authorisation, caseData, caseDataUpdated);

            //Added below fields for WA purpose
            if (ManageOrdersOptionsEnum.createAnOrder.equals(caseData.getManageOrdersOptions())
                || ManageOrdersOptionsEnum.uploadAnOrder.equals(caseData.getManageOrdersOptions())) {
                if (ManageOrdersOptionsEnum.createAnOrder.equals(caseData.getManageOrdersOptions())) {
                    orderNameForWA = caseData.getCreateSelectOrderOptions() != null
                        ? caseData.getCreateSelectOrderOptions().getDisplayedValue() : "Test";
                }
                performingUser = manageOrderService.getLoggedInUserType(authorisation);
                performingAction = caseData.getManageOrdersOptions().getDisplayedValue();
                if (null != performingUser && performingUser.equalsIgnoreCase(UserRoles.COURT_ADMIN.toString())) {
                    judgeLaReviewRequired = AmendOrderCheckEnum.judgeOrLegalAdvisorCheck
                        .equals(caseData.getManageOrders().getAmendOrderSelectCheckOptions()) ? "Yes" : "No";
                }
            }
            log.info("***performingUser***{}", performingUser);
            log.info("***performingAction***{}", performingAction);
            log.info("***judgeLaReviewRequired***{}", judgeLaReviewRequired);
            caseDataUpdated.put("performingUser", performingUser);
            caseDataUpdated.put("performingAction", performingAction);
            caseDataUpdated.put("judgeLaReviewRequired", judgeLaReviewRequired);
            caseDataUpdated.put("orderNameForWA", orderNameForWA);
            CaseUtils.setCaseState(callbackRequest, caseDataUpdated);
            cleanUpSelectedManageOrderOptions(caseDataUpdated);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }



    private static void setIsWithdrawnRequestSent(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if ((YesOrNo.No).equals(caseData.getManageOrders().getIsCaseWithdrawn())) {
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
        @RequestBody CallbackRequest callbackRequest) throws Exception {
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
            throw (new RuntimeException(INVALID_CLIENT));
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
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
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
                        caseData
                    ));
                }
                CaseData modifiedCaseData = objectMapper.convertValue(
                    caseDataUpdated,
                    CaseData.class
                );
                manageOrderService.populateServeOrderDetails(modifiedCaseData, caseDataUpdated);
            } else {
                caseDataUpdated.put(ORDERS_NEED_TO_BE_SERVED, YesOrNo.No);
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
            log.info("Manage order Before calling ref data for LA users list {}", System.currentTimeMillis());
            List<DynamicListElement> legalAdviserList = refDataUserService.getLegalAdvisorList();
            log.info("Manage order After calling ref data for LA users list {}", System.currentTimeMillis());
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
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            List<String> errorList = null;

            if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(caseData.getCreateSelectOrderOptions())) {
                //SDO - hearing screen validations
                errorList = getHearingScreenValidationsForSdo(caseData.getStandardDirectionOrder());
            } else {
                //PRL-4260 - hearing screen validations
                errorList = getHearingScreenValidations(caseData.getManageOrders().getOrdersHearingDetails(),
                                                        caseData.getCreateSelectOrderOptions(),
                                                        false);
            }

            if (isNotEmpty(errorList)) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errorList)
                    .build();
            }

            //handle preview order
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(manageOrderService.handlePreviewOrder(callbackRequest, authorisation))
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
