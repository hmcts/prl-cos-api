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
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_HEARING_DETAILS;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DraftAnOrderController {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DraftAnOrderService draftAnOrderService;

    private final HearingService hearingService;

    private final ManageOrderService manageOrderService;

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
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            return draftAnOrderService.handleSelectedOrder(callbackRequest, authorisation);
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
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(draftAnOrderService.handlePopulateDraftOrderFields(callbackRequest, authorisation)).build();
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

            List<Element<HearingData>> existingOrderHearingDetails = null;
            if (Event.DRAFT_AN_ORDER.getId().equalsIgnoreCase(callbackRequest.getEventId())) {
                existingOrderHearingDetails = caseData.getManageOrders().getOrdersHearingDetails();
            } else if (Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId()
                .equalsIgnoreCase(callbackRequest.getEventId()) || Event.EDIT_AND_APPROVE_ORDER.getId()
                .equalsIgnoreCase(callbackRequest.getEventId())) {
                DraftOrder draftOrder = draftAnOrderService.getSelectedDraftOrderDetails(caseData);
                existingOrderHearingDetails = YesOrNo.Yes.equals(draftOrder.getIsOrderCreatedBySolicitor())
                    ? caseData.getManageOrders().getSolicitorOrdersHearingDetails()
                    : caseData.getManageOrders().getOrdersHearingDetails();
                List<Element<HearingData>> hearingData = manageOrderService
                    .getHearingDataFromExistingHearingData(authorisation,
                                                           existingOrderHearingDetails,
                                                           caseData);
                if (isNotEmpty(hearingData)) {
                    caseDataUpdated.put("solicitorOrdersHearingDetails", hearingData);
                }
            }
            //check if we can move this inside first if block to avoid multiple invocations
            List<Element<HearingData>> hearingData = manageOrderService
                .getHearingDataFromExistingHearingData(authorisation,
                                                       existingOrderHearingDetails,
                                                       caseData);
            if (isNotEmpty(hearingData)) {
                caseDataUpdated.put(ORDER_HEARING_DETAILS, hearingData);
            }

            Hearings hearings = hearingService.getHearings(authorisation,
                                                           String.valueOf(callbackRequest.getCaseDetails().getId()));
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
