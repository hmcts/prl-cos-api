package uk.gov.hmcts.reform.prl.controllers.caseassignment;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CaseAssignmentService;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.exception.GrantCaseAccessException;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALLOCATED_BARRISTER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENTS;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/case-assignment")
public class CaseAssignmentController {

    private final CaseAssignmentService caseAssignmentService;
    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;
    private final AuthorisationService authorisationService;
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    private final ApplicationsTabService applicationsTabService;

    @PostMapping(path = "/barrister/add/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "About to submit to add Barrister")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse submitAddBarrister(
        @RequestHeader(javax.ws.rs.core.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseDetails caseDetails = callbackRequest.getCaseDetails();
            CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

            List<String> errorList = new ArrayList<>();
            AllocatedBarrister allocatedBarrister = caseData.getAllocatedBarrister();

            Optional<String> userId = organisationService
                .findUserByEmail(allocatedBarrister.getBarristerEmail());
            Optional<String> barristerRole  = caseAssignmentService.deriveBarristerRole(caseDetails.getData(),
                                                                                        caseData,
                                                                                        allocatedBarrister);
            caseAssignmentService.validateAddRequest(
                    userId,
                    caseData,
                    barristerRole,
                    allocatedBarrister,
                    errorList);

            if (errorList.isEmpty() && userId.isPresent() && barristerRole.isPresent()) {
                try {
                    caseAssignmentService.addBarrister(caseData,
                                                       userId.get(),
                                                       barristerRole.get(),
                                                       allocatedBarrister);
                    updateCaseDetails(caseDetails, caseData);
                } catch (GrantCaseAccessException grantCaseAccessException) {
                    errorList.add(grantCaseAccessException.getMessage());
                }
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .errors(errorList).build();
        } else {
            throw new IllegalArgumentException(INVALID_CLIENT);
        }
    }

    @PostMapping(path = "/barrister/remove/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "About to submit to remove Barrister")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse submitRemoveBarrister(
        @RequestHeader(javax.ws.rs.core.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseDetails caseDetails = callbackRequest.getCaseDetails();
            CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
            List<String> errorList = new ArrayList<>();
            AllocatedBarrister allocatedBarrister = caseData.getAllocatedBarrister();

            caseAssignmentService.validateRemoveRequest(caseData,
                                                        allocatedBarrister.getPartyList().getValueCode(),
                                                        errorList);

            if (errorList.isEmpty()) {
                PartyDetails selectedParty = caseAssignmentService.getSelectedParty(
                    caseData,
                    allocatedBarrister.getPartyList().getValueCode()
                );
                if (selectedParty != null) {
                    caseData.setAllocatedBarrister(AllocatedBarrister.builder()
                                                       .partyList(allocatedBarrister.getPartyList())
                                                       .barristerOrg(selectedParty.getBarrister().getBarristerOrg())
                                                       .barristerEmail(selectedParty.getBarrister().getBarristerEmail())
                                                       .barristerFirstName(selectedParty.getBarrister().getBarristerFirstName())
                                                       .barristerLastName(selectedParty.getBarrister().getBarristerLastName())
                                                       .build());
                }
                caseAssignmentService.removeBarrister(caseData, allocatedBarrister.getPartyList().getValueCode());
                updateCaseDetails(caseDetails, caseData);
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .errors(errorList).build();
        } else {
            throw new IllegalArgumentException(INVALID_CLIENT);
        }

    }

    private void updateCaseDetails(CaseDetails caseDetails, CaseData caseData) {
        caseDetails.getData().put(ALLOCATED_BARRISTER, caseData.getAllocatedBarrister());
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseDetails.getData().put(APPLICANTS, caseData.getApplicants());
            caseDetails.getData().put(RESPONDENTS, caseData.getRespondents());
            caseDetails.getData().putAll(applicationsTabService.updateTab(caseData));
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseDetails.getData().put(FL401_APPLICANTS, caseData.getApplicantsFL401());
            caseDetails.getData().put(FL401_RESPONDENTS, caseData.getRespondentsFL401());
            caseDetails.getData().putAll(applicationsTabService.updateTab(caseData));
        }
        String barristerFullName = caseData.getAllocatedBarrister().getBarristerFullName();
        caseDetails.getData().putAll(partyLevelCaseFlagsService
                                         .generatePartyCaseFlagsForBarristerOnly(caseData, barristerFullName));
    }
}
