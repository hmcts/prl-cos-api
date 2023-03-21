package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CitizenCaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class CaseController {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CaseService caseService;

    @Autowired
    AuthorisationService authorisationService;

    @Autowired
    ConfidentialDetailsMapper confidentialDetailsMapper;

    @Autowired
    AuthTokenGenerator authTokenGenerator;
    private static final String INVALID_CLIENT = "Invalid Client";

    @GetMapping(path = "/{caseId}", produces = APPLICATION_JSON)
    @Operation(description = "Frontend to fetch the data")
    public CaseData getCase(
        @PathVariable("caseId") String caseId,
        @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String userToken,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        CaseDetails caseDetails = null;
        if (isAuthorized(userToken, s2sToken)) {
            caseDetails = caseService.getCase(userToken, caseId);
            CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
            return caseData.toBuilder().noOfDaysRemainingToSubmitCase(
                CaseUtils.getRemainingDaysSubmitCase(caseData)).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private boolean isAuthorized(String authorisation, String s2sToken) {
        return Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation))
            && Boolean.TRUE.equals(authorisationService.authoriseService(s2sToken));
    }

    @PostMapping(value = "{caseId}/{eventId}/update-case", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Updating casedata")
    public CaseData updateCase(
        @Valid @NotNull @RequestBody CaseData caseData,
        @PathVariable("caseId") String caseId,
        @PathVariable("eventId") String eventId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestHeader("accessCode") String accessCode
    ) throws JsonProcessingException {
        if (isAuthorized(authorisation, s2sToken)) {
            CaseDetails caseDetails = null;
            String cosApis2sToken = authTokenGenerator.generate();
            caseDetails = caseService.updateCase(
                caseData,
                authorisation,
                cosApis2sToken,
                caseId,
                eventId,
                accessCode
            );
            CaseData updatedCaseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
            updatedCaseData = confidentialDetailsMapper.mapConfidentialData(updatedCaseData);
            return updatedCaseData
                .toBuilder().id(caseDetails.getId()).build();

        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @GetMapping(path = "/citizen/{role}/retrieve-cases/{userId}", produces = APPLICATION_JSON)
    public List<CaseData> retrieveCases(
        @PathVariable("role") String role,
        @PathVariable("userId") String userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        if (isAuthorized(authorisation, s2sToken)) {
            return caseService.retrieveCases(authorisation, authTokenGenerator.generate());
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @GetMapping(path = "/cases", produces = APPLICATION_JSON)
    public List<CitizenCaseData> retrieveCitizenCases(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        List<CaseData> caseDataList;
        if (isAuthorized(authorisation, s2sToken)) {
            caseDataList = caseService.retrieveCases(authorisation, authTokenGenerator.generate());
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
        return caseDataList.stream().map(this::buildCitizenCaseData).collect(Collectors.toList());
    }

    private CitizenCaseData buildCitizenCaseData(CaseData caseData) {
        return new CitizenCaseData(caseData, caseData.getState().getLabel());
    }

    @PostMapping(path = "/citizen/link", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Linking case to citizen account with access code")
    public void linkCitizenToCase(@RequestHeader("caseId") String caseId,
                                  @RequestHeader("accessCode") String accessCode,
                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                  @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken) {
        caseService.linkCitizenToCase(authorisation, s2sToken, accessCode, caseId);
    }

    @GetMapping(path = "/validate-access-code", produces = APPLICATION_JSON)
    @Operation(description = "Frontend to fetch the data")
    public String validateAccessCode(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
                                     @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
                                     @RequestHeader(value = "caseId", required = true)
                                         String caseId,
                                     @RequestHeader(value = "accessCode", required = true)
                                         String accessCode) {
        if (isAuthorized(authorisation, s2sToken)) {
            String cosApis2sToken = authTokenGenerator.generate();
            return caseService.validateAccessCode(authorisation, cosApis2sToken, caseId, accessCode);
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping("/case/create")
    @Operation(description = "Call CCD to create case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "created"),
        @ApiResponse(responseCode = "401", description = "Provided Authorization token is missing or invalid"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public CaseData createCase(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                               @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
                               @RequestBody CaseData caseData) {
        CaseDetails caseDetails = null;

        if (isAuthorized(authorisation, s2sToken)) {
            caseDetails = caseService.createCase(caseData, authorisation);
            CaseData createdCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);
            return createdCaseData.toBuilder().noOfDaysRemainingToSubmitCase(
                CaseUtils.getRemainingDaysSubmitCase(createdCaseData)).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
