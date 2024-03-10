package uk.gov.hmcts.reform.prl.controllers.hearingmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.exception.HearingManagementValidationException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDateRequest;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.hearingmanagement.HearingManagementService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingsManagementController {
    private final ObjectMapper objectMapper;
    private final AuthorisationService authorisationService;
    private final HearingManagementService hearingManagementService;
    private final AllTabServiceImpl allTabsService;

    @Value("${citizen.url}")
    private String hearingDetailsUrl;

    @PutMapping(path = "/hearing-management-state-update/{caseState}", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Ways to pay will call this API and send the status of payment with other details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public void caseStateUpdateByHearingManagement(@RequestHeader("authorization") String authorisation,
                                                   @RequestHeader("serviceAuthorization") String s2sToken,
                                                   @RequestBody HearingRequest hearingRequest,
                                                   @PathVariable("caseState") State caseState) throws Exception {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            hearingManagementService.caseStateChangeForHearingManagement(hearingRequest, caseState);
        } else {
            throw (new HearingManagementValidationException(INVALID_CLIENT));
        }
    }

    @PutMapping(path = "/hearing-management-next-hearing-date-update", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "updates the next hearing date in the prl to and send the status of update")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public void nextHearingDateUpdateByHearingManagement(@RequestHeader("authorization") String authorisation,
                                                         @RequestHeader("serviceAuthorization") String s2sToken,
                                                         @RequestBody NextHearingDateRequest nextHearingDateRequest) throws Exception {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            hearingManagementService.caseNextHearingDateChangeForHearingManagement(nextHearingDateRequest);
        } else {
            throw (new HearingManagementValidationException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/update-next-hearing-details-callback/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Update next Hearing date as part of CCD Callback")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse updateNextHearingDetailsCallback(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

            caseDataUpdated.put(
                "nextHearingDetails",
                hearingManagementService.getNextHearingDate(String.valueOf(caseData.getId()))
            );
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new HearingManagementValidationException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/update-allTabs-after-hmc-case-state/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to refresh the tabs after HMC case state update")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse updateAllTabsAfterHmcCaseState(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            allTabsService.updateAllTabsIncludingConfTab(String.valueOf(callbackRequest.getCaseDetails().getId()));
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        } else {
            throw (new HearingManagementValidationException(INVALID_CLIENT));
        }
    }
}
