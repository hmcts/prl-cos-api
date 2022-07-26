package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
public class CaseController {

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CaseService caseService;

    @GetMapping(path = "/{caseId}", produces = APPLICATION_JSON)
    @ApiOperation(value = "Frontend to fetch the data")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case details returned", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public CaseData serviceRequestUpdate(
        @PathVariable("caseId") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken,
        @RequestHeader("serviceAuthorization") String s2sToken
    ) {
        return objectMapper.convertValue(
            coreCaseDataApi.getCase(userToken, s2sToken, caseId).getData(),
            CaseData.class
        );
    }

    @PostMapping(value = "{caseId}/{eventId}/update-case", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation("Updates case")
    public CaseData updateCase(
        @Valid @NotNull @RequestBody CaseData caseData,
        @PathVariable("caseId") String caseId,
        @PathVariable("eventId") String eventId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader("serviceAuthorization") String s2sToken
    ) {
        return objectMapper.convertValue(caseService.updateCase(caseData,
                                                                authorisation,
                                                                s2sToken,
                                                                caseId,
                                                                eventId).getData(), CaseData.class);
    }

    @GetMapping(path = "/citizen/{role}/retrieve-cases/{userId}", produces = APPLICATION_JSON)
    @ApiOperation(value = "Frontend to fetch case list")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case details returned", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public List<CaseData> retrieveCases(
        @PathVariable("role") String role,
        @PathVariable("userId") String userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader("serviceAuthorization") String s2sToken
    ) {
        return caseService.retrieveCases(authorisation, s2sToken, role, userId);
    }

    @PutMapping("/citizen/link")
    @ApiOperation("Links citizen to case")
    public void linkDefendantToClaim(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                     @RequestHeader(value = "caseId", required = false) String caseId,
                                     @RequestHeader("serviceAuthorization") String s2sToken,
                                     @RequestHeader("accessCode") String accessCode) {
        caseService.linkCitizenToCase(authorisation, s2sToken, accessCode ,caseId);
    }

}
