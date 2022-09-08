package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
public class CaseController {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CaseService caseService;
    @Autowired
    AuthorisationService authorisationService;
    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @GetMapping(path = "/{caseId}", produces = APPLICATION_JSON)
    @Operation(description = "Frontend to fetch the data")
    public CaseData getCase(
        @PathVariable("caseId") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        CaseDetails caseDetails = null;
        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authToken))
            && Boolean.TRUE.equals(authorisationService.authoriseService(s2sToken))) {
            caseDetails = caseService.getCase(authToken, authTokenGenerator.generate(), caseId);
        } else {
            throw (new RuntimeException("Invalid Client"));
        }

        return objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder().id(caseDetails.getId()).build();
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
    ) {
        CaseDetails caseDetails = null;
        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation))
            && Boolean.TRUE.equals(authorisationService.authoriseService(s2sToken))) {
            caseDetails = caseService.updateCase(caseData, authorisation, authTokenGenerator.generate(), caseId, eventId, accessCode);
        } else {
            throw (new RuntimeException("Invalid Client"));
        }

        return objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder().id(caseDetails.getId()).build();
    }

    @GetMapping(path = "/citizen/{role}/retrieve-cases/{userId}", produces = APPLICATION_JSON)
    public List<CaseData> retrieveCases(
        @PathVariable("role") String role,
        @PathVariable("userId") String userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        List<CaseData> caseDataList;
        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation))
            && Boolean.TRUE.equals(authorisationService.authoriseService(s2sToken))) {
            caseDataList = caseService.retrieveCases(authorisation, authTokenGenerator.generate(), role, userId);
        } else {
            throw (new RuntimeException("Invalid Client"));
        }

        return caseDataList;
    }

    @GetMapping(path = "/cases", produces = APPLICATION_JSON)
    public List<CaseData> retrieveCitizenCases(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken
    ) {
        List<CaseData> caseDataList;
        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation))
            && Boolean.TRUE.equals(authorisationService.authoriseService(s2sToken))) {
            caseDataList = caseService.retrieveCases(authorisation, authTokenGenerator.generate());
        } else {
            throw (new RuntimeException("Invalid Client"));
        }
        return caseDataList;
    }

    @PostMapping(path = "/citizen/link", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Linking case to citizen account with access code")
    public void linkCitizenToCase(@RequestHeader("caseId") String caseId,
                                  @RequestHeader("accessCode") String accessCode,
                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                  @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken) {
        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation))
            && Boolean.TRUE.equals(authorisationService.authoriseService(s2sToken))) {
            caseService.linkCitizenToCase(authorisation, authTokenGenerator.generate(), accessCode, caseId);
        } else {
            throw (new RuntimeException("Invalid Client"));
        }
    }

    @GetMapping(path = "/validate-access-code", produces = APPLICATION_JSON)
    @Operation(description = "Frontend to fetch the data")
    public String validateAccessCode(@RequestHeader(value = "Authorization", required = true) String authorisation,
                                     @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
                                     @RequestHeader(value = "caseId", required = true) String caseId,
                                     @RequestHeader(value = "accessCode", required = true) String accessCode) {
        String accessCodeStatus;
        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation))
            && Boolean.TRUE.equals(authorisationService.authoriseService(s2sToken))) {
            accessCodeStatus = caseService.validateAccessCode(authorisation, authTokenGenerator.generate(), caseId, accessCode);
        } else {
            throw (new RuntimeException("Invalid Client"));
        }
        return accessCodeStatus;
    }
}
