package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenCaseUpdateService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenCaseUpdateController {
    private final ObjectMapper objectMapper;
    private final CitizenCaseUpdateService citizenCaseUpdateService;
    private final AuthorisationService authorisationService;
    private static final String INVALID_CLIENT = "Invalid Client";

    @PostMapping(value = "{caseId}/{eventId}/citizen-update-party-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Processing citizen updates")
    public CaseData updatePartyDetailsFromCitizen(
        @NotNull @Valid @RequestBody CitizenUpdatedCaseData citizenUpdatedCaseData,
        @PathVariable("eventId") String eventId,
        @PathVariable("caseId") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            try {
                log.info("*** printing case data" + objectMapper.writeValueAsString(
                    citizenUpdatedCaseData));
            } catch (JsonProcessingException e) {
                log.info("error");
            }
            CaseDetails caseDetails = citizenCaseUpdateService.updateCitizenPartyDetails(
                authorisation,
                caseId,
                eventId,
                citizenUpdatedCaseData
            );
            return CaseUtils.getCaseData(caseDetails, objectMapper);
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
