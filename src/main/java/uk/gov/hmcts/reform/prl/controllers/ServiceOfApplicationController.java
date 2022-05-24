package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationEmailService;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api
@RestController
@RequestMapping("/service-of-application")
@Slf4j
public class ServiceOfApplicationController {


    @Autowired
    private ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Serve Parties Email Notification")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        log.info("send Email Notification To Server Parties, On \"Service Of Application\" Event Triggered");
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        serviceOfApplicationEmailService.sendEmail(caseDetails);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


}
