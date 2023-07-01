package uk.gov.hmcts.reform.prl.controllers.restrictedcaseaccess;

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
import uk.gov.hmcts.reform.prl.enums.restrictedcaseaccess.CaseSecurityClassification;
import uk.gov.hmcts.reform.prl.services.RestrictedCaseAccessService;

import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequestMapping("/restricted-case-access")
@RequiredArgsConstructor
public class RestrictedCaseAccessController {
    @Autowired
    private final RestrictedCaseAccessService restrictedCaseAccessService;

    @PostMapping(path = "/mark-as-restricted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse markAsRestricted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
        restrictedCaseAccessService.markAsRestricted(callbackRequest, authorisation);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    @PostMapping(path = "/mark-as-restricted-1", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse markAsRestricted1(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> securityUpdated = new HashMap<>();
        securityUpdated.put("securityClassification", CaseSecurityClassification.RESTRICTED);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .securityClassification(securityUpdated)
            .build();
    }

    @PostMapping(path = "/mark-as-restricted-2", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse markAsRestricted2(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> securityUpdated = new HashMap<>();
        securityUpdated.put("security_Classification", CaseSecurityClassification.RESTRICTED);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .securityClassification(securityUpdated)
            .build();
    }

    @PostMapping(path = "/mark-as-restricted-3", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse markAsRestricted3(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("security_Classification", CaseSecurityClassification.RESTRICTED);
        Map<String, Object> securityUpdated = new HashMap<>();
        securityUpdated.put("security_Classification", CaseSecurityClassification.RESTRICTED);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .securityClassification(securityUpdated)
            .build();
    }

    @PostMapping(path = "/mark-as-restricted-4", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse markAsRestricted4(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("security_Classification", CaseSecurityClassification.RESTRICTED);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }

    @PostMapping(path = "/mark-as-restricted-5", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse markAsRestricted5(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("securityClassification", CaseSecurityClassification.RESTRICTED);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }

    @PostMapping(path = "/mark-as-restricted-6", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse markAsRestricted6(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("securityClassification", CaseSecurityClassification.RESTRICTED);
        Map<String, Object> securityUpdated = new HashMap<>();
        securityUpdated.put("securityClassification", CaseSecurityClassification.RESTRICTED);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .securityClassification(securityUpdated)
            .build();
    }
}

