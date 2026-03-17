package uk.gov.hmcts.reform.prl.controllers;

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
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ExitAwaitingInformationService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExitAwaitingInformationController {

    private final ExitAwaitingInformationService exitAwaitingInformationService;
    private final AuthorisationService authorisationService;
    private final FeatureToggleService featureToggleService;

    @PostMapping(path = "/submit-exit-awaiting-information", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Exit awaiting information callback to update case data and set case status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse submitExitAwaitingInformation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        log.info("Submitting exit awaiting information for case: {}",
            callbackRequest.getCaseDetails().getId());

        if (authorisationService.isAuthorized(authorisation, s2sToken)
            && featureToggleService.isExitAwaitingInformationEnabled()) {
            var caseDataUpdated = exitAwaitingInformationService.updateCase(callbackRequest);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        }
        throw new RuntimeException(INVALID_CLIENT);
    }

}
