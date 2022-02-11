package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.ReturnApplicationService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequiredArgsConstructor
public class ReturnApplicationReturnMessageController {

    @Autowired
    private UserService userService;
    @Autowired
    private ReturnApplicationService returnApplicationService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private final CaseWorkerEmailService caseWorkerEmailService;

    @PostMapping(path = "/return-application-return-message", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to get return message of the return application ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback proceeded"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse returnApplicationReturnMessage(
        @RequestHeader("Authorization") String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws WorkflowException {

        UserDetails userDetails = userService.getUserDetails(authorisation);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class)
            .toBuilder()
            .id(callbackRequest.getCaseDetails().getId())
            .build();

        if (!returnApplicationService.noRejectReasonSelected(caseData)) {
            caseDataUpdated.put("returnMessage", returnApplicationService.getReturnMessage(caseData, userDetails));
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/return-application-notification", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to send return application email notification")
    public AboutToStartOrSubmitCallbackResponse returnApplicationEmailNotification(
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        caseWorkerEmailService.sendReturnApplicationEmailToSolicitor(callbackRequest.getCaseDetails());

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
