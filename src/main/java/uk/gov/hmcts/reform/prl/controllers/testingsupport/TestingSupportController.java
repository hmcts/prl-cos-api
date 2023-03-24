package uk.gov.hmcts.reform.prl.controllers.testingsupport;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TestingSupportService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


@Slf4j
@RestController
@RequestMapping("/testing-support")
@RequiredArgsConstructor

public class TestingSupportController {

    @Autowired
    private final TestingSupportService testingSupportService;
    @Autowired
    ObjectMapper objectMapper;

    @PostMapping(path = "/about-to-submit-case-creation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Initiate the case creation for testing support")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse aboutToSubmitCaseCreation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
        return AboutToStartOrSubmitCallbackResponse.builder().data(testingSupportService.initiateCaseCreation(authorisation,
            callbackRequest
        )).build();
    }

    @PostMapping(path = "/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Initiate the case creation for testing support")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse submittedCaseCreation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        return AboutToStartOrSubmitCallbackResponse.builder().data(testingSupportService.submittedCaseCreation(
            callbackRequest
        )).build();
    }
    @PostMapping(path = "/about-to-submit-case-creation-citizen", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Initiate the citizen case creation for testing support")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "processed.",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public CaseData aboutToSubmitCaseCreation_citizen(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CaseData caseData
    ) throws Exception {
        CaseDetails caseDetails = null;
            caseDetails = testingSupportService.initiateCaseCreation_citizen();
            CaseData createdCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);
            return createdCaseData.toBuilder().noOfDaysRemainingToSubmitCase(
                CaseUtils.getRemainingDaysSubmitCase(createdCaseData)).build();
        }
    }

