package uk.gov.hmcts.reform.prl.controllers.closingcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.closingcase.ClosingCaseService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class ClosingCaseController extends AbstractCallbackController {
    private final ClosingCaseService closingCaseService;
    private final AuthorisationService authorisationService;

    @Autowired
    public ClosingCaseController(ObjectMapper objectMapper, EventService eventPublisher,
                                 ClosingCaseService closingCaseService,
                                 AuthorisationService authorisationService) {
        super(objectMapper, eventPublisher);
        this.closingCaseService = closingCaseService;
        this.authorisationService = authorisationService;
    }

    @PostMapping(path = "/closing-case/pre-populate-child-data", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate Hearing page details")
    public AboutToStartOrSubmitCallbackResponse prePopulateChildData(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(closingCaseService
                          .prePopulateChildData(callbackRequest))
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/closing-case/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "List On Notice submission flow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List on notice submission is success"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse populateSelectedChildWithFinalOutcome(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(closingCaseService.populateSelectedChildWithFinalOutcome(callbackRequest)).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/closing-case/validate-child-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "List On Notice submission flow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List on notice submission is success"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse validateChildDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(callbackRequest.getCaseDetails().getData())
                .errors(closingCaseService.validateChildDetails(callbackRequest)).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/closing-case/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "List On Notice submission flow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List on notice submission is success"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse closingCaseAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(closingCaseService.closingCaseForChildren(callbackRequest)).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
