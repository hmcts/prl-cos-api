package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
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

@Api
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
    @ApiOperation(value = "Callback for add case number submit event")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
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
    @ApiOperation(value = "Serve Parties Email Notification")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = serviceOfApplicationService.sendEmail(callbackRequest.getCaseDetails());
        Map<String,Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        updatedCaseData.put("respondentCaseInvites",caseData.getRespondentCaseInvites());
        Map<String, Object> allTabsFields = allTabService.getAllTabsFields(caseData);
        updatedCaseData.putAll(allTabsFields);
        return AboutToStartOrSubmitCallbackResponse.builder().data(updatedCaseData).build();
    }
}
