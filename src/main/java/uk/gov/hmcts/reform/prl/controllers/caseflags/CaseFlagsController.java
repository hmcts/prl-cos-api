package uk.gov.hmcts.reform.prl.controllers.caseflags;

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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsWaService;
import uk.gov.hmcts.reform.prl.services.caseflags.FlagsService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_ALL_SELECTED_FLAGS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_IS_CASE_FLAG_TASK_CREATED;

@Slf4j
@RestController
@RequestMapping("/caseflags")
public class CaseFlagsController extends AbstractCallbackController {
    private static final String REQUESTED = "Requested";

    private final AuthorisationService authorisationService;
    private final CaseFlagsWaService caseFlagsWaService;
    private final FlagsService flagsService;

    @Autowired
    public CaseFlagsController(ObjectMapper objectMapper,
                               EventService eventPublisher,
                               AuthorisationService authorisationService,
                               CaseFlagsWaService caseFlagsWaService,
                               FlagsService flagsService) {
        super(objectMapper, eventPublisher);
        this.authorisationService = authorisationService;
        this.caseFlagsWaService = caseFlagsWaService;
        this.flagsService = flagsService;
    }

    @PostMapping(path = "/setup-wa-task", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to check and create WA task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public void setUpWaTaskForCaseFlags2(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            caseFlagsWaService.setUpWaTaskForCaseFlagsEventHandler(
                authorisation,
                callbackRequest
            );
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/check-wa-task-status", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to check the work allocation status to decide if the task should be created or not")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse checkWorkAllocationTaskStatus(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = checkIfNewFlagRequireToCreateWaTask(callbackRequest);
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        caseDataMap.put(WA_IS_CASE_FLAG_TASK_CREATED, caseData.getReviewRaRequestWrapper().getIsCaseFlagsTaskCreated());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataMap)
            .build();
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestHeader("Authorization")
                                                                   @Parameter(hidden = true) String authorisation,
                                                                   @RequestBody CallbackRequest callbackRequest) {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseFlagsWaService.setSelectedFlags(caseData);

        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        caseDataMap.put(WA_ALL_SELECTED_FLAGS, caseData.getReviewRaRequestWrapper().getSelectedFlags());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataMap)
            .build();
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
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Element<FlagDetail> mostRecentlyModified = caseFlagsWaService.validateAllFlags(caseData);

        List<String> errors = new ArrayList<>();
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        if (REQUESTED.equals(mostRecentlyModified.getValue().getStatus())) {
            errors.add("Please select status other than Requested");
        } else {
            caseFlagsWaService.searchAndUpdateCaseFlags(caseData, caseDataMap, mostRecentlyModified);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseDataMap)
            .build();
    }

    @PostMapping("/submitted")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmitted(@RequestHeader("Authorization")
                                                                         @Parameter(hidden = true) String authorisation,
                                                                     @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseFlagsWaService.checkAllRequestedFlagsAndCloseTask(caseData);
        return ok(SubmittedCallbackResponse.builder().build());
    }

    @PostMapping("/submitted-to-close-wa-task")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmittedToCloseWaTask(@RequestHeader("Authorization")
                                                                     @Parameter(hidden = true) String authorisation,
                                                                     @RequestBody CallbackRequest callbackRequest) {
        return handleSubmitted(authorisation, callbackRequest);
    }


    @PostMapping(path = "/review-lang-sm/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to set selected case note")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartForReviewLangSm(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestHeader(value = PrlAppsConstants.CLIENT_CONTEXT_HEADER_PARAMETER, required = false) String clientContext,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
            flagsService.prepareSelectedReviewLangAndSmReq(caseDataMap, clientContext);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataMap).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/review-lang-sm/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to validate newly added flag status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitForReviewLangSm(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Map<String, Object> caseDataCurrent = callbackRequest.getCaseDetails().getData();
            List<String> errors = flagsService.validateNewFlagStatus(caseDataCurrent);
            if (errors.isEmpty()) {
                CaseData caseData = checkIfNewFlagRequireToCreateWaTask(callbackRequest);
                caseDataCurrent.put(WA_IS_CASE_FLAG_TASK_CREATED, caseData.getReviewRaRequestWrapper().getIsCaseFlagsTaskCreated());
            }
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataCurrent).errors(errors).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private CaseData checkIfNewFlagRequireToCreateWaTask(CallbackRequest callbackRequest) {
        CaseData caseDataBefore = CaseUtils.getCaseData(callbackRequest.getCaseDetailsBefore(), objectMapper);
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseFlagsWaService.checkCaseFlagsToCreateTask(caseData, caseDataBefore);
        return caseData;
    }
}
