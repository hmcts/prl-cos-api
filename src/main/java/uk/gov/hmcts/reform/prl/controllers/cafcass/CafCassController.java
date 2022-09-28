package uk.gov.hmcts.reform.prl.controllers.cafcass;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.cafcass.CaseDataService;

import java.io.IOException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
public class CafCassController extends AbstractCallbackController {

    @Autowired
    private CaseDataService caseDataService;

    @Autowired
    private AuthorisationService authorisationService;

    @GetMapping(path = "/searchCases", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "search case data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search cases processed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity searcCasesByDates(
        @RequestHeader("authorisation") String authorisation,
        @RequestHeader("serviceAuthorisation") String serviceAuthorisation,
        @RequestParam(name = "start_date") String startDate,  @RequestParam(name = "end_date") String endDate
    ) throws IOException {

        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation)) && Boolean.TRUE.equals(
            authorisationService.authoriseService(serviceAuthorisation))) {
            log.info("processing  request after authorization");
            return ResponseEntity.ok(caseDataService.getCaseData(authorisation, serviceAuthorisation, startDate, endDate));

        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

    }
}
