package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.exception.CoreCaseDataStoreException;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.citizen.awp.CitizenAwpRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.C100AwpProcessHwfPaymentService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenCaseUpdateService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/citizen")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenCaseUpdateController {
    private final ObjectMapper objectMapper;
    private final CitizenCaseUpdateService citizenCaseUpdateService;
    private final AuthorisationService authorisationService;
    private static final String INVALID_CLIENT = "Invalid Client";
    private final CaseService caseService;

    private final C100AwpProcessHwfPaymentService c100AwpProcessHwfPaymentService;

    @PostMapping(value = "/{caseId}/{eventId}/update-party-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Processing citizen updates")
    public CaseDataWithHearingResponse updatePartyDetailsFromCitizen(
        @NotNull @Valid @RequestBody CitizenUpdatedCaseData citizenUpdatedCaseData,
        @PathVariable("eventId") String eventId,
        @PathVariable("caseId") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseDetails caseDetails = citizenCaseUpdateService.updateCitizenPartyDetails(
                authorisation,
                caseId,
                eventId,
                citizenUpdatedCaseData
            );
            if (caseDetails != null) {
                return caseService
                    .getCaseDataWithHearingResponse(authorisation,"Yes", caseDetails);
            } else {
                log.error("{} event has failed for the case {}", eventId, caseId);
                throw new CoreCaseDataStoreException("Citizen party update failed for this transaction");
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(value = "/{caseId}/save-c100-draft-application")
    @Operation(description = "Processing c100 draft save and come back later updates")
    public CaseData saveDraftCitizenApplication(
        @PathVariable("caseId") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @Valid @NotNull @RequestBody CaseData caseData
    ) throws JsonProcessingException {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseDetails caseDetails = citizenCaseUpdateService.saveDraftCitizenApplication(
                caseId,
                caseData,
                authorisation
            );
            if (caseDetails != null) {
                return CaseUtils.getCaseData(caseDetails, objectMapper);
            } else {
                log.error("saveDraftCitizenApplication has failed for the case {}", caseId);
                throw new CoreCaseDataStoreException("Citizen save c100 draft application failed for this transaction");
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(value = "/{caseId}/{eventId}/submit-c100-application")
    @Operation(description = "Processing c100 case submission updates")
    public CaseData submitC100Application(
        @PathVariable("caseId") String caseId,
        @PathVariable("eventId") String eventId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @Valid @NotNull @RequestBody CaseData caseData
    ) throws JsonProcessingException {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseDetails caseDetails = citizenCaseUpdateService.submitCitizenC100Application(
                authorisation,
                caseId,
                eventId,
                caseData
            );
            if (caseDetails != null) {
                log.info("submitC100Application is successful for the case {}", caseId);
                return CaseUtils.getCaseData(caseDetails, objectMapper);
            } else {
                log.error("submitC100Application has failed for the case {}", caseId);
                throw new CoreCaseDataStoreException("Citizen submit c100  application failed for this transaction");
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(value = "/{caseId}/delete-application")
    @Operation(description = "Processing citizen updates")
    public CaseData deleteApplicationCitizen(
        @PathVariable("caseId") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @Valid @NotNull @RequestBody CaseData caseData
    ) throws JsonProcessingException {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseDetails caseDetails = citizenCaseUpdateService.deleteApplication(
                caseId,
                caseData,
                authorisation
            );
            if (caseDetails != null) {
                return CaseUtils.getCaseData(caseDetails, objectMapper);
            } else {
                log.error("deleteApplicationCitizen is not successful for the case {}", caseId);
                throw new CoreCaseDataStoreException("Citizen delete application failed for this transaction");
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(value = "/{caseId}/withdraw", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Withdraw a case submitted by citizen")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "success"),
        @ApiResponse(responseCode = "401", description = "Provided Authorization token is missing or invalid"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public CaseData withdrawCase(
        @Valid @NotNull @RequestBody CaseData caseData,
        @PathVariable("caseId") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseDetails caseDetails = citizenCaseUpdateService.withdrawCase(caseData, caseId, authorisation);
            if (caseDetails != null) {
                return CaseUtils.getCaseData(caseDetails, objectMapper);
            } else {
                log.error("withdrawCase is not successful for the case {}", caseId);
                throw new CoreCaseDataStoreException("Citizen withdraw application failed for this transaction");
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(value = "{caseId}/save-citizen-awp-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Save citizen awp application into case data")
    public ResponseEntity<Object> saveCitizenAwpApplication(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                            @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
                                                            @PathVariable("caseId") String caseId,
                                                            @Valid @NotNull @RequestBody CitizenAwpRequest citizenAwpRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            log.info("*** Inside saveCitizenAwpApplication -> citizen awp request  {}", citizenAwpRequest);
            CaseDetails caseDetails = citizenCaseUpdateService.saveCitizenAwpApplication(authorisation, caseId, citizenAwpRequest);

            if (null != caseDetails) {
                return ResponseEntity.ok("Success");
            } else {
                return ResponseEntity.internalServerError().body("Error happened in saving citizen awp application");
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(value = "/awp-process-hwf-payment", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Save citizen awp application into case data")
    public void processAwpWithHwfPayment() {
        c100AwpProcessHwfPaymentService.checkHwfPaymentStatusAndUpdateApplicationStatus();
    }
}
