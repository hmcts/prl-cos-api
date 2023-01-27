package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SecurityRequirement(name = "Bearer Authentication")
public class TestController {

    @PostMapping(path = "/test-confirmation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Confirmation page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resubmission completed"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<SubmittedCallbackResponse> ccdSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        log.info("Before {}", callbackRequest);
        return ok(SubmittedCallbackResponse.builder().confirmationHeader("TEST").confirmationBody(
            "YOU ARE SEEING CONFIRMATION PAGE").build());
    }
}
