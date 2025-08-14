package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.request.CitizenPartyFlagsRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.LanguageSupportCaseNotesRequest;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenCaseUpdateService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReasonableAdjustmentsController {

    private final CaseService caseService;
    private final AuthorisationService authorisationService;
    private final CitizenCaseUpdateService citizenCaseUpdateService;
    private static final String INVALID_CLIENT = "Invalid Client";

    @PostMapping(value = "{caseId}/{eventId}/party-update-ra", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Update party flags for citizen")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Updated party flags for citizen"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Not found")})
    public ResponseEntity<Object> updateCitizenRAflags(
        @NotNull @RequestBody CitizenPartyFlagsRequest citizenPartyFlagsRequest,
        @PathVariable("eventId") String eventId,
        @PathVariable("caseId") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        log.info("Inside updateCitizenRAflags controller");
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return caseService.updateCitizenRAflags(
                caseId,
                eventId,
                authorisation,
                citizenPartyFlagsRequest
            );
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized");
        }
    }

    @GetMapping(path = "/{caseId}/retrieve-ra-flags/{partyId}", produces = APPLICATION_JSON)
    @Operation(description = "Frontend to fetch RA flags for the given party")
    public Flags getCaseFlags(
        @PathVariable("caseId") String caseId,
        @PathVariable("partyId") String partyId,
        @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return caseService.getPartyCaseFlags(authorisation, caseId, partyId);
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(value = "{caseId}/language-support-notes", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Update party flags for citizen")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Updated party flags for citizen"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Not found")})
    public ResponseEntity<Object> languageSupportCaseNotes(
        @NotNull @RequestBody LanguageSupportCaseNotesRequest languageSupportCaseNotesRequest,
        @PathVariable("caseId") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        log.info("Inside updateCitizenRAflags controller");
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return citizenCaseUpdateService.addLanguageSupportCaseNotes(
                caseId,
                authorisation,
                languageSupportCaseNotesRequest
            );
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized");
        }
    }
}
