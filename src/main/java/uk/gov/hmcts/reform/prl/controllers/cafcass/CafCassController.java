package uk.gov.hmcts.reform.prl.controllers.cafcass;

import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.exception.cafcass.exceptionhandlers.ApiError;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.cafcass.CaseDataService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.status;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@Slf4j
@RestController
@RequestMapping("/cases")
public class CafCassController extends AbstractCallbackController {

    @Autowired
    private CaseDataService caseDataService;

    @Autowired
    private AuthorisationService authorisationService;

    private static final String BEARER = "Bearer ";

    @GetMapping(path = "/searchCases", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "search case data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search cases processed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity searcCasesByDates(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestParam(name = "start_date") String startDate,  @RequestParam(name = "end_date") String endDate
    )  {
        try {
            if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation))) {
                serviceAuthorisation = serviceAuthorisation.startsWith(BEARER)
                    ? serviceAuthorisation : BEARER.concat(" " + serviceAuthorisation);
                if (Boolean.TRUE.equals(authorisationService.authoriseService(serviceAuthorisation))) {
                    log.info("processing request after authorization");
                    return ResponseEntity.ok(caseDataService.getCaseData(
                        authorisation,
                        startDate,
                        endDate
                    ));
                } else {
                    if (StringUtils.isEmpty(serviceAuthorisation)) {
                        log.info("S2s token is null or empty");
                    } else {
                        log.info("S2s token is not null. However, its not valid");
                    }

                    log.info("S2s token is not unauthorized");
                    throw new ResponseStatusException(FORBIDDEN);
                }
            } else {
                log.info("auth token is not unauthorized");
                throw new ResponseStatusException(UNAUTHORIZED);
            }

        } catch (ResponseStatusException e) {
            return status(UNAUTHORIZED).body(new ApiError(e.getMessage()));
        } catch (FeignException feignException) {
            return status(feignException.status()).body(new ApiError(feignException.getMessage()));
        } catch (Exception e) {
            return status(INTERNAL_SERVER_ERROR).body(new ApiError(e.getMessage()));
        }
    }
}
