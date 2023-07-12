package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequestMapping("/service-of-application")
@Slf4j
public class ServiceOfApplicationController {

    @Autowired
    private ServiceOfApplicationService serviceOfApplicationService;

    @PostMapping(path = "/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "about to start callback for service of application event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackRequest
    ) {
        return AboutToStartOrSubmitCallbackResponse.builder().data(serviceOfApplicationService.getSoaCaseFieldsMap(
            callbackRequest.getCaseDetails())).build();
    }

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "about to submit callback for service of application event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {

        return AboutToStartOrSubmitCallbackResponse.builder().data(serviceOfApplicationService.handleAboutToSubmit(callbackRequest)).build();
    }

    @PostMapping(path = "/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "submitted callback for service of application event.Serve Parties Email and Post Notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        return serviceOfApplicationService
            .handleSoaSubmitted(authorisation, callbackRequest);

    }
}
