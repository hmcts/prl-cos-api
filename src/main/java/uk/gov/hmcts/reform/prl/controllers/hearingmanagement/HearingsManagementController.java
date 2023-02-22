package uk.gov.hmcts.reform.prl.controllers.hearingmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.exception.HearingManagementValidationException;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDateRequest;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.hearingmanagement.HearingManagementService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class HearingsManagementController {

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CaseService caseService;

    @Autowired
    private IdamClient idamClient;

    @Autowired
    private AuthorisationService authorisationService;

    @Autowired
    private SystemUserService systemUserService;

    @Autowired
    private HearingManagementService hearingManagementService;

    @Value("${citizen.url}")
    private String hearingDetailsUrl;

    @PutMapping(path = "/hearing-management-state-update/{caseState}", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Ways to pay will call this API and send the status of payment with other details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public void caseStateUpdateByHearingManagement(@RequestHeader("serviceAuthorization") String s2sToken,
                                                   @RequestBody HearingRequest hearingRequest,
                                                   @PathVariable("caseState") State caseState) throws Exception {

        if (Boolean.FALSE.equals(authorisationService.authoriseService(s2sToken))) {
            throw new HearingManagementValidationException("Provide a valid s2s token");
        } else {
            hearingManagementService.caseStateChangeForHearingManagement(hearingRequest,caseState);
        }
    }

    @PutMapping(path = "/hearing-management-next-hearing-date-update", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "updates the next hearing date in the prl to and send the status of update")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public void nextHearingDateUpdateByHearingManagement(@RequestHeader("authorization") String authorization,
                                                         @RequestHeader("serviceAuthorization") String s2sToken,
                                                         @RequestBody NextHearingDateRequest nextHearingDateRequest) throws Exception {
        if (Boolean.FALSE.equals(authorisationService.authoriseUser(authorization)) && Boolean.FALSE.equals(authorisationService.authoriseService(s2sToken))) {
            throw new HearingManagementValidationException("Provide a valid s2s token");
        } else {
            hearingManagementService.caseNextHearingDateChangeForHearingManagement(nextHearingDateRequest);
        }

    }
}
