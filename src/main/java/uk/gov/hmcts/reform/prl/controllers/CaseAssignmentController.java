package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
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
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALLOCATED_BARRISTER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/case-assignment")
public class CaseAssignmentController {

    private final CaseAssignmentService caseAssignmentService;
    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;
    private final AuthorisationService authorisationService;
    private final AllTabServiceImpl allTabService;

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
            AllocatedBarrister allocatedBarrister = objectMapper.convertValue(
                caseDetails.getData().get(ALLOCATED_BARRISTER),
                new TypeReference<>() { }
            );

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
                caseAssignmentService.addBarrister(caseData,
                                                   userId.get(),
                                                   barristerRole.get(),
                                                   allocatedBarrister);
            }

            Map<String, Object> caseDataMap = caseData.toMap(objectMapper);
            caseDataMap.putAll(allTabService.getAllTabsFields(caseData));

            if (caseDataMap.containsKey(CASE_TYPE_OF_APPLICATION) && caseDataMap.get(CASE_TYPE_OF_APPLICATION) == null) {
                caseDataMap.put(CASE_TYPE_OF_APPLICATION, caseData.getSelectedCaseTypeID());
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataMap)
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
            AllocatedBarrister allocatedBarrister = objectMapper.convertValue(
                caseDetails.getData().get(ALLOCATED_BARRISTER),
                new TypeReference<>() { }
            );
            if (allocatedBarrister != null && allocatedBarrister.getPartyList() != null) {
                caseAssignmentService.validateRemoveRequest(
                    caseData,
                    allocatedBarrister.getPartyList().getValueCode(),
                    errorList
                );

                if (errorList.isEmpty()) {
                    caseAssignmentService.removeBarrister(
                        caseData,
                        allocatedBarrister.getPartyList().getValueCode()
                    );
                }
            } else {
                errorList.add(INVALID_CLIENT);
            }

            Map<String, Object> caseDataMap = caseData.toMap(objectMapper);
            caseDataMap.putAll(allTabService.getAllTabsFields(caseData));

            if (caseDataMap.containsKey(CASE_TYPE_OF_APPLICATION) && caseDataMap.get(CASE_TYPE_OF_APPLICATION) == null) {
                caseDataMap.put(CASE_TYPE_OF_APPLICATION, caseData.getSelectedCaseTypeID());
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataMap)
                .errors(errorList).build();
        } else {
            throw new IllegalArgumentException(INVALID_CLIENT);
        }

    }
}
