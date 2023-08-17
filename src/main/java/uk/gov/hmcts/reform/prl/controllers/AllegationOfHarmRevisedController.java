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
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AllegationOfHarmRevisedService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AllegationOfHarmRevisedController {

    private final ObjectMapper objectMapper;

    private final AllegationOfHarmRevisedService allegationOfHarmRevisedService;

    @PostMapping(path = "/pre-populate-child-data", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "pre populuates child data ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse prePopulateChildData(@RequestHeader(HttpHeaders.AUTHORIZATION)
                                                                     @Parameter(hidden = true) String authorisation,
                                                                     @RequestBody uk.gov.hmcts.reform
                                                                             .ccd.client.model.CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        caseDataUpdated.putAll(allegationOfHarmRevisedService.getPrePopulatedChildData(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

}
