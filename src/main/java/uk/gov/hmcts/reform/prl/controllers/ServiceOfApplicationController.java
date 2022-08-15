package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequestMapping("/service-of-application")
@Slf4j
public class ServiceOfApplicationController {

    @Autowired
    private ServiceOfApplicationService serviceOfApplicationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    AllTabServiceImpl allTabService;


    @PostMapping(path = "/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for add case number submit event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackRequest
    ) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        if (caseData.getOrderCollection() != null && !caseData.getOrderCollection().isEmpty()) {
            List<String> createdOrders = caseData.getOrderCollection().stream()
                .map(Element::getValue).map(OrderDetails::getOrderType)
                .collect(Collectors.toList());
            caseDataUpdated = serviceOfApplicationService.getOrderSelectionsEnumValues(createdOrders,caseDataUpdated);
        }
        caseDataUpdated.put("sentDocumentPlaceHolder", serviceOfApplicationService.getCollapsableOfSentDocuments());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Serve Parties Email Notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = serviceOfApplicationService.sendEmail(callbackRequest.getCaseDetails());
        serviceOfApplicationService.sendPost(callbackRequest.getCaseDetails(), authorisation);
        Map<String,Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        updatedCaseData.put("respondentCaseInvites",caseData.getRespondentCaseInvites());
        Map<String, Object> allTabsFields = allTabService.getAllTabsFields(caseData);
        updatedCaseData.putAll(allTabsFields);
        return AboutToStartOrSubmitCallbackResponse.builder().data(updatedCaseData).build();
    }
}
