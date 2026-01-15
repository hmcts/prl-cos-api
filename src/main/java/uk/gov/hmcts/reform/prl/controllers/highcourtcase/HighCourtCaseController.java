package uk.gov.hmcts.reform.prl.controllers.highcourtcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;

import java.util.ArrayList;
import java.util.List;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@RequestMapping("/high-court-case")
public class HighCourtCaseController  extends AbstractCallbackController {

    private final AuthorisationService authorisationService;

    @Autowired
    public HighCourtCaseController(ObjectMapper objectMapper, EventService eventPublisher,
                                   AuthorisationService authorisationService) {
        super(objectMapper, eventPublisher);
        this.authorisationService = authorisationService;
    }



    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to check the work allocation status to decide if the task should be closed or not")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            final CaseDetails caseDetails = callbackRequest.getCaseDetails();


            List<String> errors = new ArrayList<>();
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .data(caseDetails.getData())
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }


    @PostMapping("/submitted")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            return ok(SubmittedCallbackResponse.builder().build());
        }  else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

}
