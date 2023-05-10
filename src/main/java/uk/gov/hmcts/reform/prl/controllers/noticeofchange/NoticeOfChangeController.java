package uk.gov.hmcts.reform.prl.controllers.noticeofchange;

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
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/noc")
public class NoticeOfChangeController extends AbstractCallbackController {

    @Autowired
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;

    @PostMapping(path = "/aboutToSubmitNoCRequest", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "About to submit NoC Request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse aboutToSubmitNoCRequest(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        return noticeOfChangePartiesService.applyDecision(callbackRequest, authorisation);
    }

    @PostMapping(path = "/submittedNoCRequest", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Submitted request for NoC")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public void submittedNoCRequest(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        noticeOfChangePartiesService.nocRequestSubmitted(callbackRequest, authorisation);
    }

    @PostMapping(path = "/aboutToStartStopRepresentation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "About to Start solicitor stop representation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse aboutToStartStopRepresentation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        List<String> errorList = new ArrayList<>();
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(noticeOfChangePartiesService.populateAboutToStartStopRepresentation(
                authorisation,
                callbackRequest,
                errorList
            )).errors(errorList).build();
    }
}
