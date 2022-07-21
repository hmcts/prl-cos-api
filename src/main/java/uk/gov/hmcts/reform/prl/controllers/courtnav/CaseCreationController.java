package uk.gov.hmcts.reform.prl.controllers.courtnav;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseCreationResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.courtnav.CaseCreationService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CaseCreationController {

    private static final String SERVICE_AUTH = "ServiceAuthorization";

    private final CaseCreationService caseCreationService;
    private final AuthorisationService authorisationService;

    @PostMapping(path = "/case", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Third party to call this service to create a case in CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case is created", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity createCase(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTH) String serviceAuthorization,
        @RequestBody CaseData inputData
    ) {
        if (Boolean.TRUE.equals(authorisationService.authorise(serviceAuthorization))) {
            CaseDetails caseDetails = caseCreationService.createCourtNavCase(
                authorisation,
                inputData
            );
            return ResponseEntity.ok().body(new CaseCreationResponse(String.valueOf(caseDetails.getId())));
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
