package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WARNING_TEXT_DIV;

@RestController
@RequestMapping("/confidentiality-check")
@Slf4j
@RequiredArgsConstructor
public class ConfidentialityCheckController {

    public static final String NO_PACKS_AVAILABLE_FOR_CONFIDENTIAL_DETAILS_CHECK = "There are no packs available for confidential details check";

    private final ServiceOfApplicationService serviceOfApplicationService;

    private final ObjectMapper objectMapper;

    public static final String CONFIDENTIALITY_CHECK_WARNING_TEXT = "confidentialityCheckWarningText";

    public static final String CONFIDENTIALITY_CHECK_WARNING_TEXT_MESSAGE = WARNING_TEXT_DIV
        + "</span><strong class='govuk-warning-text__text'>You need to check the confidential details tab and review the"
        + " service packs in the service of application tab before continuing.</strong></div>";

    @PostMapping(path = "/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Confidentiality check about to start event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse confidentialCheckAboutToStart(
        @RequestBody CallbackRequest callbackRequest
    ) {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("caseData.getServiceOfApplication() {}", caseData.getServiceOfApplication());

        log.info(
            "Object utils caseData.getServiceOfApplication() {}",
            ObjectUtils.isEmpty(caseData.getServiceOfApplication().getUnServedApplicantPack())
        );

        if (CaseUtils.unServedPacksPresent(caseData)) {
            log.info("Packs present to serve");
            Map<String, Object> caseDataUpdated = new HashMap<>();
            caseDataUpdated.put(CONFIDENTIALITY_CHECK_WARNING_TEXT, CONFIDENTIALITY_CHECK_WARNING_TEXT_MESSAGE);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of(
            NO_PACKS_AVAILABLE_FOR_CONFIDENTIAL_DETAILS_CHECK)).build();
    }

    @PostMapping(path = "/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Confidentiality check submitted event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmittedNew(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        log.info("inside new confidential check submitted event");

        return serviceOfApplicationService.processConfidentialityCheck(authorisation, callbackRequest);
    }
}
