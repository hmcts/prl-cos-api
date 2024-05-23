package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.exception.CoreCaseDataStoreException;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.DocumentRequest;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenResponseService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@RequestMapping("/citizen")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenResponseController {
    private final AuthorisationService authorisationService;
    private final CitizenResponseService citizenResponseService;
    private final CaseService caseService;

    @PostMapping(path = "/{caseId}/{partyId}/generate-c7document", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @Operation(description = "Generate a PDF for citizen as part of Respond to the Application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document generated"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public Document generateC7DraftDocument(
        @PathVariable("caseId") String caseId,
        @PathVariable("partyId") String partyId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody DocumentRequest documentRequest,
        @RequestHeader("serviceAuthorization") String s2sToken) throws Exception {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            return citizenResponseService.generateAndReturnDraftC7(caseId, partyId, authorisation,
                                                                   documentRequest.isWelsh());
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/{caseId}/submit-citizen-response", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @Operation(description = "Submit C7 response and generate all docs for citizen respondent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document generated"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public CaseDataWithHearingResponse submitAndGenerateC7(
            @NotNull @Valid @RequestBody CitizenUpdatedCaseData citizenUpdatedCaseData,
            @PathVariable("caseId") String caseId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
            @RequestHeader("serviceAuthorization") String s2sToken) throws Exception {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseDetails caseDetails = citizenResponseService.generateAndSubmitCitizenResponse(authorisation, caseId, citizenUpdatedCaseData);
            if (caseDetails != null) {
                return caseService
                    .getCaseDataWithHearingResponse(authorisation,"Yes", caseDetails);
            } else {
                log.error("{} event has failed for the case {}", CaseEvent.REVIEW_AND_SUBMIT, caseId);
                throw new CoreCaseDataStoreException("Citizen party update failed for this transaction");
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}

