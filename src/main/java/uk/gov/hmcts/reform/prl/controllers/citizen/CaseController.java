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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CitizenCaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.UiCitizenCaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.TASK_LIST_V3_FLAG;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseController {

    private final ObjectMapper objectMapper;
    private final HearingService hearingService;
    private final CaseService caseService;
    private final AuthorisationService authorisationService;
    private final ConfidentialDetailsMapper confidentialDetailsMapper;
    private final AuthTokenGenerator authTokenGenerator;
    private static final String INVALID_CLIENT = "Invalid Client";
    private final LaunchDarklyClient launchDarklyClient;
    private static final String CASE_LINKING_FAILED = "Case Linking has failed";

    @GetMapping(path = "/{caseId}", produces = APPLICATION_JSON)
    @Operation(description = "Frontend to fetch the data")
    public UiCitizenCaseData getCase(
        @PathVariable("caseId") String caseId,
        @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String userToken,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        if (authorisationService.isAuthorized(userToken, s2sToken)) {
            CaseDetails caseDetails = caseService.getCase(userToken, caseId);
            CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
            return UiCitizenCaseData.builder()
                .caseData(caseData.toBuilder()
                              .noOfDaysRemainingToSubmitCase(
                                  CaseUtils.getRemainingDaysSubmitCase(caseData))
                              .build())
                .citizenDocumentsManagement(caseService.getAllCitizenDocumentsOrders(userToken, caseData))
                .build();

        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @GetMapping(path = "/retrieve-case-and-hearing/{caseId}/{hearingNeeded}", produces = APPLICATION_JSON)
    @Operation(description = "Frontend to fetch the data")
    public CaseDataWithHearingResponse retrieveCaseWithHearing(
        @PathVariable("caseId") String caseId,
        @PathVariable("hearingNeeded") String hearingNeeded,
        @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            return caseService.getCaseWithHearing(authorisation, caseId, hearingNeeded);
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
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
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseDetails caseDetails = caseService.updateCase(
                caseData,
                authorisation,
                caseId,
                eventId
            );
            CaseData updatedCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);
            updatedCaseData = confidentialDetailsMapper.mapConfidentialData(updatedCaseData, true);

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
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
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
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            caseDataList = caseService.retrieveCases(authorisation, authTokenGenerator.generate());
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
        return caseDataList.stream().map(this::buildCitizenCaseData).toList();
    }

    private CitizenCaseData buildCitizenCaseData(CaseData caseData) {
        return new CitizenCaseData(caseData, caseData.getState().getLabel());
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
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())
                && launchDarklyClient.isFeatureEnabled(TASK_LIST_V3_FLAG)) {
                caseData = caseData.toBuilder()
                    .taskListVersion(TASK_LIST_VERSION_V3)
                    .build();
            }
            CaseDetails caseDetails = caseService.createCase(caseData, authorisation);
            CaseData createdCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);
            return createdCaseData.toBuilder().noOfDaysRemainingToSubmitCase(
                PrlAppsConstants.CASE_SUBMISSION_THRESHOLD).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(value = "/hearing/{caseId}", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Get hearing details for a case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "success"),
        @ApiResponse(responseCode = "401", description = "Provided Authorization token is missing or invalid"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public Hearings getAllHearingsForCitizenCase(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @PathVariable("caseId") String caseId) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            return hearingService.getHearings(authorisation, caseId);
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @GetMapping(value = "/fetchIdam-Am-roles/{emailId}", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Get hearing details for a case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "success"),
        @ApiResponse(responseCode = "401", description = "Provided Authorization token is missing or invalid"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public Map<String, String> fetchIdamAmRoles(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @PathVariable("emailId") String emailId) {
        boolean isAuthorised = authorisationService.authoriseUser(authorisation);
        if (isAuthorised) {
            return caseService.fetchIdamAmRoles(authorisation, emailId);
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
