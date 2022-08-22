package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.prl.services.cafcass.CaseDataService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CafCassController extends AbstractCallbackController {
    @Autowired
    private CaseDataService caseDataService;

    @PutMapping(path = "/searchCases", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search cases processed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public void searcCasesByDates(
        @RequestParam(name = "start_date") String startDate,  @RequestParam(name = "end_date") String endDate
    ) {
        caseDataService.getCaseData(startDate, endDate);
    }
}
