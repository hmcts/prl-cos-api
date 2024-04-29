package uk.gov.hmcts.reform.prl.controllers.noticeofchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@RequestMapping("/noc")
public class NoticeOfChangeController extends AbstractCallbackController {
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;
    private final AuthorisationService authorisationService;

    @Autowired
    protected NoticeOfChangeController(ObjectMapper objectMapper, EventService eventPublisher,
                                       NoticeOfChangePartiesService
        noticeOfChangePartiesService, AuthorisationService authorisationService) {
        super(objectMapper, eventPublisher);
        this.noticeOfChangePartiesService = noticeOfChangePartiesService;
        this.authorisationService = authorisationService;
    }

    @PostMapping(path = "/aboutToSubmitNoCRequest", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "About to submit NoC Request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse aboutToSubmitNoCRequest(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return noticeOfChangePartiesService.applyDecision(callbackRequest, authorisation);
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/submittedNoCRequest", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Submitted request for NoC")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public void submittedNoCRequest(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws JsonProcessingException {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            ObjectMapper om = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            String result = om.writeValueAsString(callbackRequest.getCaseDetails().getData());
            System.out.println("RRRRRRRRRRRR ==> " + result);
            noticeOfChangePartiesService.nocRequestSubmitted(callbackRequest);
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/aboutToStartStopRepresentation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "About to start solicitor stop representation")
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

    @PostMapping(path = "/aboutToSubmitStopRepresentation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "About to submit solicitor stop representation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse aboutToSubmitStopRepresentation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        List<String> errorList = new ArrayList<>();
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(noticeOfChangePartiesService.aboutToSubmitStopRepresenting(authorisation, callbackRequest
            )).errors(errorList).build();
    }

    @PostMapping(path = "/submittedStopRepresentation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Submitted solicitor stop representation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public void submittedStopRepresentation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        noticeOfChangePartiesService.submittedStopRepresenting(callbackRequest);
    }

    @PostMapping(path = "/aboutToStartAdminRemoveLegalRepresentative", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "About to start solicitor stop representation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse aboutToStartAdminRemoveLegalRepresentative(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        List<String> errorList = new ArrayList<>();
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(noticeOfChangePartiesService.populateAboutToStartAdminRemoveLegalRepresentative(
                callbackRequest,
                errorList
            )).errors(errorList).build();
    }

    @PostMapping(path = "/aboutToSubmitAdminRemoveLegalRepresentative", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "About to submit solicitor stop representation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse aboutToSubmitAdminRemoveLegalRepresentative(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        List<String> errorList = new ArrayList<>();
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(noticeOfChangePartiesService.aboutToSubmitAdminRemoveLegalRepresentative(authorisation, callbackRequest
            )).errors(errorList).build();
    }

    @PostMapping(path = "/submittedAdminRemoveLegalRepresentative", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Submitted solicitor stop representation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SubmittedCallbackResponse> submittedAdminRemoveLegalRepresentative(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        return ok(noticeOfChangePartiesService.submittedAdminRemoveLegalRepresentative(callbackRequest));
    }

}
