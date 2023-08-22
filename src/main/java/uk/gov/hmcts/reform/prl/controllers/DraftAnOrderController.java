package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_HEARING_DETAILS;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DraftAnOrderController {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ManageOrderService manageOrderService;

    @Autowired
    private DraftAnOrderService draftAnOrderService;

    @Autowired
    private HearingDataService hearingDataService;

    private final HearingService hearingService;

    @Autowired
    private DynamicMultiSelectListService dynamicMultiSelectListService;

    @Autowired
    private AuthorisationService authorisationService;

    @PostMapping(path = "/reset-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to reset fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback to reset fields"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse resetFields(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse.builder().data(draftAnOrderService.resetFields(callbackRequest)).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/selected-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public CallbackResponse populateHeader(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );

            caseData = caseData.toBuilder()
                .selectedOrder(null != caseData.getCreateSelectOrderOptions()
                                   ? caseData.getCreateSelectOrderOptions().getDisplayedValue() : "")
                .build();
            caseData = caseData.toBuilder().caseTypeOfApplication(CaseUtils.getCaseTypeOfApplication(caseData)).build();
            ManageOrders manageOrders = caseData.getManageOrders();
            manageOrders = manageOrders.toBuilder().childOption(DynamicMultiSelectList.builder()
                                                     .listItems(dynamicMultiSelectListService.getChildrenMultiSelectList(
                                                         caseData)).build()).build();

            if (null != caseData.getCreateSelectOrderOptions()
                && CreateSelectOrderOptionsEnum.blankOrderOrDirections.equals(caseData.getCreateSelectOrderOptions())) {
                manageOrders.setTypeOfC21Order(null != manageOrders.getC21OrderOptions()
                                                   ? manageOrders.getC21OrderOptions().getDisplayedValue() : null);
            }
            List<String> errorList = new ArrayList<>();
            if (ChildArrangementOrdersEnum.standardDirectionsOrder.getDisplayedValue().equalsIgnoreCase(caseData.getSelectedOrder())) {
                errorList.add("This order is not available to be drafted");
                return CallbackResponse.builder()
                    .errors(errorList)
                    .build();
            } else if (ChildArrangementOrdersEnum.directionOnIssueOrder.getDisplayedValue().equalsIgnoreCase(caseData.getSelectedOrder())) {
                errorList.add("This order is not available to be drafted");
                return CallbackResponse.builder()
                    .errors(errorList)
                    .build();
            } else {
                //PRL-3254 - Populate hearing details dropdown for create order
                DynamicList hearingsDynamicList = manageOrderService.populateHearingsDropdown(authorisation, caseData);
                manageOrders = manageOrders.toBuilder().hearingsType(hearingsDynamicList).build();
                caseData = caseData.toBuilder().manageOrders(manageOrders).build();
                return CallbackResponse.builder()
                    .data(caseData).build();
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/populate-draft-order-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateFl404Fields(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            ManageOrders manageOrders = caseData.getManageOrders();
            if (null != manageOrders) {
                manageOrders = manageOrders.toBuilder()
                    .childOption(caseData.getManageOrders().getChildOption())
                    .build();
            }

            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            caseDataUpdated.put("caseTypeOfApplication", CaseUtils.getCaseTypeOfApplication(caseData));
            String caseReferenceNumber = String.valueOf(callbackRequest.getCaseDetails().getId());
            Hearings hearings = hearingService.getHearings(authorisation, caseReferenceNumber);
            HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
                hearingDataService.populateHearingDynamicLists(authorisation, caseReferenceNumber, caseData, hearings);
            caseDataUpdated.put(
                ORDER_HEARING_DETAILS,
                ElementUtils.wrapElements(
                    hearingDataService.generateHearingData(
                        hearingDataPrePopulatedDynamicLists, caseData))
            );
            if (!(CreateSelectOrderOptionsEnum.blankOrderOrDirections.equals(caseData.getCreateSelectOrderOptions()))
                && PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            ) {
                caseData = manageOrderService.populateCustomOrderFields(caseData);
                if (Objects.nonNull(caseData.getManageOrders())) {
                    caseDataUpdated.putAll(caseData.getManageOrders().toMap(CcdObjectMapper.getObjectMapper()));
                }
                if (Objects.nonNull(caseData.getSelectedOrder())) {
                    caseDataUpdated.put("selectedOrder", caseData.getSelectedOrder());
                }
                if (Objects.nonNull(caseData.getStandardDirectionOrder())) {
                    caseDataUpdated.putAll(caseData.getStandardDirectionOrder().toMap(CcdObjectMapper.getObjectMapper()));
                }
            } else {
                caseData = draftAnOrderService.generateDocument(callbackRequest, caseData);
                if (Objects.nonNull(caseData.getStandardDirectionOrder())) {
                    caseDataUpdated.putAll(caseData.getStandardDirectionOrder().toMap(CcdObjectMapper.getObjectMapper()));
                }
                if (Objects.nonNull(caseData.getManageOrders())) {
                    caseDataUpdated.putAll(caseData.getManageOrders().toMap(CcdObjectMapper.getObjectMapper()));

                }
                caseDataUpdated.put("appointedGuardianName",caseData.getAppointedGuardianName());
                caseDataUpdated.put("dateOrderMade",caseData.getDateOrderMade());
                CaseData caseData1 = caseData.toBuilder().build();
                caseDataUpdated.putAll(manageOrderService.getCaseData(
                    authorisation,
                    caseData1,
                    caseData.getCreateSelectOrderOptions()
                ));
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated).build();
        }  else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/populate-standard-direction-order-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate standard direction order fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateSdoFields(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            if (DraftAnOrderService.checkStandingOrderOptionsSelected(caseData)) {
                draftAnOrderService.populateStandardDirectionOrderDefaultFields(authorisation, caseData, caseDataUpdated);
            } else {
                List<String> errorList = new ArrayList<>();
                errorList.add(
                    "Please select at least one options from below");
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errorList)
                    .build();
            }
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/populate-direction-on-issue", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate direction on issue fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateDioFields(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            if (DraftAnOrderService.checkDirectionOnIssueOptionsSelected(caseData)) {
                draftAnOrderService.populateDirectionOnIssueFields(authorisation, caseData, caseDataUpdated);
            } else {
                List<String> errorList = new ArrayList<>();
                errorList.add(
                    "Please select at least one options from below");
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errorList)
                    .build();
            }
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/generate-doc", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse generateDoc(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            String caseReferenceNumber = String.valueOf(callbackRequest.getCaseDetails().getId());
            List<Element<HearingData>> existingOrderHearingDetails = null;
            Hearings hearings = hearingService.getHearings(authorisation, caseReferenceNumber);
            HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
                hearingDataService.populateHearingDynamicLists(authorisation, caseReferenceNumber, caseData, hearings);
            if (Event.DRAFT_AN_ORDER.getId().equalsIgnoreCase(callbackRequest.getEventId())) {
                existingOrderHearingDetails = caseData.getManageOrders().getOrdersHearingDetails();
            } else if (Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId()
                .equalsIgnoreCase(callbackRequest.getEventId()) || Event.EDIT_AND_APPROVE_ORDER.getId()
                .equalsIgnoreCase(callbackRequest.getEventId())) {
                DraftOrder draftOrder = draftAnOrderService.getSelectedDraftOrderDetails(caseData);
                existingOrderHearingDetails = YesOrNo.Yes.equals(draftOrder.getIsOrderCreatedBySolicitor())
                    ? caseData.getManageOrders().getSolicitorOrdersHearingDetails()
                    : caseData.getManageOrders().getOrdersHearingDetails();
                if (null != existingOrderHearingDetails) {
                    caseDataUpdated.put(
                        "solicitorOrdersHearingDetails",
                        hearingDataService.getHearingData(existingOrderHearingDetails,
                                                          hearingDataPrePopulatedDynamicLists, caseData
                        )
                    );
                }
            }
            if (existingOrderHearingDetails != null) {
                caseDataUpdated.put(
                    ORDER_HEARING_DETAILS,
                    hearingDataService.getHearingData(existingOrderHearingDetails,
                                                      hearingDataPrePopulatedDynamicLists, caseData
                    )
                );
            }
            caseDataUpdated.putAll(draftAnOrderService.generateOrderDocument(
                authorisation,
                callbackRequest,
                hearings
            ));
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to generate draft order collection")
    public AboutToStartOrSubmitCallbackResponse prepareDraftOrderCollection(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {


            return AboutToStartOrSubmitCallbackResponse.builder().data(draftAnOrderService.prepareDraftOrderCollection(
                authorisation,
                callbackRequest
            )).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
