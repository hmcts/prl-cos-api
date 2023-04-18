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
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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
    private DynamicMultiSelectListService dynamicMultiSelectListService;

    @PostMapping(path = "/reset-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to reset fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback to reset fields"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse resetFields(
        @RequestBody CallbackRequest callbackRequest) {
        return AboutToStartOrSubmitCallbackResponse.builder().data(Collections.emptyMap()).build();
    }

    @PostMapping(path = "/selected-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
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
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("selectedOrder", caseData.getCreateSelectOrderOptions() != null
            ? caseData.getCreateSelectOrderOptions().getDisplayedValue() : "");

        caseDataUpdated.put("childOption", DynamicMultiSelectList.builder()
            .listItems(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).build());

        if (caseDataUpdated.get("selectedOrder") == "Standard directions order") {
            List<String> errorList = new ArrayList<>();
            errorList.add(
                "Solicitors cannot draft a Standard Directions order");
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errorList)
                .build();
        } else if (caseDataUpdated.get("selectedOrder") == "Direction on issue") {
            List<String> errorList = new ArrayList<>();
            errorList.add(
                "Solicitors cannot draft a Direction On Issue order");
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errorList)
                .build();
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated).build();
        }
    }

    @PostMapping(path = "/populate-draft-order-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateFl404Fields(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
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

        log.info("ChildOption Data in populateFl404Fields {} start ", caseDataUpdated.get("childOption"));

        if (!(CreateSelectOrderOptionsEnum.blankOrderOrDirections.equals(caseData.getCreateSelectOrderOptions()))
            && PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
        ) {
            caseData = manageOrderService.populateCustomOrderFields(caseData);
        } else {
            log.info("Before generate document manageorder {}", caseData.getManageOrders());
            caseData = draftAnOrderService.generateDocument(callbackRequest, caseData);
            log.info("Before generate document casedata {}", caseData.getManageOrders());
            caseDataUpdated.putAll(manageOrderService.getCaseData(authorisation, caseData, caseData.getCreateSelectOrderOptions()));
        }
        if (caseData != null) {
            caseDataUpdated.putAll(caseData.toMap(CcdObjectMapper.getObjectMapper()));
        }

        log.info("childrenListForDocmosis Data in populateFl404Fields {} end ", caseDataUpdated.get("childrenListForDocmosis"));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated).build();
    }

    @PostMapping(path = "/populate-standard-direction-order-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate standard direction order fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateSdoFields(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (DraftAnOrderService.checkStandingOrderOptionsSelected(caseData)) {
            draftAnOrderService.populateStandardDirectionOrderFields(authorisation, caseData, caseDataUpdated);
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
    }

    @PostMapping(path = "/populate-direction-on-issue", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate direction on issue fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateDioFields(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
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
    }

    @PostMapping(path = "/generate-doc", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse generateDoc(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
        return AboutToStartOrSubmitCallbackResponse.builder().data(draftAnOrderService.generateOrderDocument(
            authorisation,
            callbackRequest
        )).build();
    }

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to generate draft order collection")
    public AboutToStartOrSubmitCallbackResponse prepareDraftOrderCollection(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        return AboutToStartOrSubmitCallbackResponse.builder().data(draftAnOrderService.prepareDraftOrderCollection(
            authorisation,
            callbackRequest
        )).build();
    }
}
