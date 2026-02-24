package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.AwaitingInformation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.AwaitingInformationService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AwaitingInformationController {
    private final AwaitingInformationService awaitingInformationService;
    private final ObjectMapper objectMapper;
    private final AuthorisationService authorisationService;
    private final FeatureToggleService featureToggleService;

    @PostMapping(path = "/submit-awaiting-information", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Awaiting On Information callback to update case data and set case status to awaiting information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse submitAwaitingInformation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)
            && featureToggleService.isAwaitingInformationEnabled()) {
            Map<String, Object> caseDataUpdated = awaitingInformationService.addToCase(callbackRequest);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        }
        throw (new RuntimeException(INVALID_CLIENT));
    }

    @PostMapping(path = "/populate-header-awaiting-information", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header awaiting information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback to populate the header awaiting information Processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateHeader(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)
            && featureToggleService.isAwaitingInformationEnabled()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .build();
        }
        throw (new RuntimeException(INVALID_CLIENT));

    }

    @PostMapping(path = "/validate-awaiting-information", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to validate review date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Child details are fetched"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public CallbackResponse validateUrgentCaseCreation(
        @RequestBody CallbackRequest callbackRequest
    ) {

        if (featureToggleService.isAwaitingInformationEnabled()) {
            AwaitingInformation awaitingInformation = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(), AwaitingInformation.class);
            List<String> errorList = awaitingInformationService.validate(awaitingInformation);

            return CallbackResponse.builder()
                .errors(errorList)
                .build();
        }
        throw new RuntimeException(INVALID_CLIENT);
    }
}
