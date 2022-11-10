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
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.exception.HearingManagementValidationException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.HearingDetailsRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.hearingmanagement.HearingManagementService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

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

    @PutMapping(path = "/hearing-management-state-update", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Ways to pay will call this API and send the status of payment with other details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public void hearingManagementStateUpdate(@RequestHeader("serviceAuthorization") String s2sToken,
                                     @RequestBody HearingRequest hearingRequest) throws Exception {

        if (Boolean.FALSE.equals(authorisationService.authoriseService(s2sToken))) {
            throw new HearingManagementValidationException("Provide a valid s2s token");
        } else {
            hearingManagementService.stateChangeForHearingManagement(hearingRequest);
        }

    }

    @PostMapping(path = "/hearing-details-notification", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
    @Operation(description = "Send a notification to the parties about the hearing date.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification Sent"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public String sendHearingNotification(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                          @RequestHeader("serviceAuthorization") String s2sToken,
                                          @ModelAttribute HearingDetailsRequest hearingDetailsRequest) throws Exception {

        String caseId = hearingDetailsRequest.getCaseId();
        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        String eventId = hearingDetailsRequest.getEventId();
        String newState;
        caseData = caseData.toBuilder()
            .state(State.DECISION_OUTCOME)
            .build();

        hearingManagementService.sendHearingDetailsEmailToCitizen(hearingDetailsRequest, caseData);

        return "SUCCESS";
    }


}
