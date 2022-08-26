package uk.gov.hmcts.reform.prl.controllers.cafcaas;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.services.cafcass.CaseDataService;

import java.io.IOException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CafCassController extends AbstractCallbackController {
    @Autowired
    private CaseDataService caseDataService;

    @GetMapping(path = "/searchCases", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search cases processed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity searcCasesByDates(
        @RequestHeader("authorisation") String authorisation,
        @RequestHeader("serviceAuthorisation") String serviceAuthorisation,
        @RequestParam(name = "start_date") String startDate,  @RequestParam(name = "end_date") String endDate
    ) throws IOException {
        return ResponseEntity.ok(caseDataService.getCaseData(authorisation, serviceAuthorisation, startDate, endDate));
    }
}
