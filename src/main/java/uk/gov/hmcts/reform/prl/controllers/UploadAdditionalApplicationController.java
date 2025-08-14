package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UploadAdditionalApplicationService;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UploadAdditionalApplicationController {
    private final UploadAdditionalApplicationService uploadAdditionalApplicationService;
    private final AuthorisationService authorisationService;
    private final SystemUserService systemUserService;

    @PostMapping(path = "/pre-populate-applicants", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Generate applicants")
    public AboutToStartOrSubmitCallbackResponse prePopulateApplicants(@RequestHeader("Authorization")
                                                                      @Parameter(hidden = true) String authorisation,
                                                                      @RequestBody CallbackRequest callbackRequest,
                                                                      @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(uploadAdditionalApplicationService.prePopulateApplicants(
                          callbackRequest,
                          authorisation
                      )
                ).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }


    @PostMapping(path = "/upload-additional-application/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to create additional application bundle ")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Bundle created"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse createUploadAdditionalApplicationBundle(@RequestHeader("Authorization")
                                                                                        @Parameter(hidden = true) String authorisation,
                                                                                        @RequestBody CallbackRequest callbackRequest,
                                                                                        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER)
                                                                                        String s2sToken) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            String systemAuthorisation = systemUserService.getSysUserToken();
            Map<String, Object> caseDataUpdated
                = uploadAdditionalApplicationService.createUploadAdditionalApplicationBundle(
                systemAuthorisation,
                authorisation,
                callbackRequest
            );

            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/upload-additional-application/mid-event/calculate-fee", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to calculate fees for additional applications ")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Bundle created"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse calculateAdditionalApplicationsFee(@RequestHeader("Authorization")
                                                                                   @Parameter(hidden = true) String authorisation,
                                                                                   @RequestBody CallbackRequest callbackRequest,
                                                                                   @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER)
                                                                                   String s2sToken) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(uploadAdditionalApplicationService.calculateAdditionalApplicationsFee(
                    authorisation,
                    callbackRequest
                )).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/upload-additional-application/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to calculate fees for additional applications ")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Bundle created"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<SubmittedCallbackResponse> uploadAdditionalApplicationSubmittedEvent(@RequestHeader("Authorization")
                                                                                               @Parameter(hidden = true) String authorisation,
                                                                                               @RequestBody CallbackRequest callbackRequest,
                                                                                               @RequestHeader
                                                                                                   (PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER)
                                                                                               String s2sToken) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            return ok(uploadAdditionalApplicationService.uploadAdditionalApplicationSubmitted(callbackRequest));
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/upload-additional-application/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Generate applicants")
    public AboutToStartOrSubmitCallbackResponse populateHearingList(@RequestHeader("Authorization")
                                                                    @Parameter(hidden = true) String authorisation,
                                                                    @RequestBody CallbackRequest callbackRequest,
                                                                    @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            String systemAuthorisation = systemUserService.getSysUserToken();
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(uploadAdditionalApplicationService.populateHearingList(systemAuthorisation, callbackRequest))
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
