package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ServiceOfDocumentsService;

import java.util.HashMap;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@RestController
@RequestMapping("/service-of-documents")
@Slf4j
@RequiredArgsConstructor
public class ServiceOfDocumentsController {

    private final AuthorisationService authorisationService;
    private final ServiceOfDocumentsService serviceOfDocumentsService;

    @PostMapping(path = "/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "about to start callback for service of documents event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(new HashMap<>())//serviceOfDocumentsService.getSoaCaseFieldsMap(authorisation, callbackRequest.getCaseDetails()))
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
