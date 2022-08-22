package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;

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
    @Operation(description = "Frontend to fetch the data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case details returned", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = uk.gov.hmcts.reform.ccd.client.model.CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
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
    @Operation(description = "Updates case")
    public CaseData updateCase(
        @Valid @NotNull @RequestBody CaseData caseData,
        @PathVariable("caseId") String caseId,
        @PathVariable("eventId") String eventId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader("serviceAuthorization") String s2sToken
    ) {
        return objectMapper.convertValue(caseService.updateCase(
            caseData,
            authorisation,
            s2sToken,
            caseId,
            eventId
        ).getData(), CaseData.class);
    }

    @PostMapping("/case/create")
    @Operation(description = "Call CCD to create case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "created"),
        @ApiResponse(responseCode = "401", description = "Provided Authorization token is missing or invalid"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public CaseData createCase(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                               @RequestHeader("serviceAuthorization") String s2sToken,
                               @RequestBody CaseData caseData) {

        CaseDetails caseDetails = caseService.createCase(caseData, authorisation, s2sToken);
        return objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder().id(caseDetails.getId()).build();
    }

}
