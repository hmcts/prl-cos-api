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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CourtSealFinderService;
import uk.gov.hmcts.reform.prl.services.EventService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@RestController
@RequestMapping("/high-court-case")
public class HighCourtCaseController  extends AbstractCallbackController {


    private final CourtSealFinderService courtSealFinderService;

    public HighCourtCaseController(ObjectMapper objectMapper, EventService eventPublisher, CourtSealFinderService courtSealFinderService) {
        super(objectMapper, eventPublisher);
        this.courtSealFinderService = courtSealFinderService;
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

        log.info("High court case about-to-submit");
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("about-to-submit IsHighCourtCase {}", caseData.getIsHighCourtCase());


        if (caseData.getIsHighCourtCase() == Yes) {
            caseDataUpdated.put("courtSeal", courtSealFinderService.getHighCourtSeal());
        } else {
            caseDataUpdated.put("courtSeal", courtSealFinderService.getCourtSeal(caseData.getCourtId()));
        }

        List<String> errors = new ArrayList<>();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseDataUpdated)
            .build();
    }


    @PostMapping("/submitted")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmitted(@RequestHeader("Authorization")
                                                                     @Parameter(hidden = true) String authorisation,
                                                                     @RequestBody CallbackRequest callbackRequest) {
        log.info("High court case submitted");
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        log.info("submitted IsHighCourtCase {}", caseData.getIsHighCourtCase());
        return ok(SubmittedCallbackResponse.builder().build());
    }





    private Map<String, Object> printCaseDataReceivedFromXui(CallbackRequest callbackRequest) {
        final Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        if (nonNull(data)) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (nonNull(entry.getKey()) && nonNull(entry.getValue())) {
                    log.info("High Court Case data map {} ", entry.getKey() + " = " + entry.getValue());
                } else if (nonNull(entry.getKey())) {
                    log.info("High Court Case data map {} has null value", entry.getKey());
                } else {
                    log.info("High Court Case data map has null key for value {}", entry.getValue().toString());
                }
            }
        } else {
            log.info("High Court Case data map is null");
        }
        return data;
    }


}
