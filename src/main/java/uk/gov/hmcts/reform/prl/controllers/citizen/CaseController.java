package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
public class CaseController {

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    ObjectMapper objectMapper;

    @GetMapping(path = "/{caseId}", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Frontend to fetch the data")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case details returned", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public CaseData serviceRequestUpdate(
        @PathVariable("caseId") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken,
        @RequestHeader("serviceAuthorization") String s2sToken
    ) {
        return objectMapper.convertValue(coreCaseDataApi.getCase(userToken, s2sToken, caseId).getData(),
                                         CaseData.class);
    }
}
