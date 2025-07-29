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
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CaseAssignmentService;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/case-assignment")
public class CaseAssignmentController {

    private final CaseAssignmentService ccdCaseAssignmentService;
    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;

    @PostMapping(path = "/aboutToSubmitAddBarrister", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "About to submit to add Barrister")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse submitAddBarrister(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        AllocatedBarrister allocatedBarrister = caseData.getAllocatedBarrister();
        //TODO derive barrister role to add from the case data
        String roleItem = allocatedBarrister.getRoleItem();

        return getAboutToStartOrSubmitCallbackResponse(caseData,
                                                       (userId,errorList) ->
                ccdCaseAssignmentService.validateAddRequest(caseData,
                                                            roleItem,
                                                            errorList),
                                                       userId ->
            ccdCaseAssignmentService.grantBarristerCaseAccess(caseData,
                                                              userId,
                                                              roleItem));
    }

    @PostMapping(path = "/aboutToSubmitRemoveBarrister", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "About to submit to remove Barrister")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse submitRemoveBarrister(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        AllocatedBarrister allocatedBarrister = caseData.getAllocatedBarrister();
        //TODO derive barrister role to remove from the case data
        String roleItem = allocatedBarrister.getRoleItem();
        //TODO retrieve org id from the case data
        String organisationId = allocatedBarrister.getBarristerOrg().getOrganisationID();
        return getAboutToStartOrSubmitCallbackResponse(caseData,
                                                       (userId,errorList) ->
                                                           ccdCaseAssignmentService.validateRemoveRequest(caseData,
                                                                                                          userId,
                                                                                                          roleItem,
                                                                                                          errorList),
                                                       userId ->
            ccdCaseAssignmentService.removeCaseAccess(caseData,
                                                      userId,
                                                      roleItem,
                                                      organisationId));
    }

    private AboutToStartOrSubmitCallbackResponse getAboutToStartOrSubmitCallbackResponse(CaseData caseData,
                                                                                         BiConsumer<String, List<String>> validator,
                                                                                         Consumer<String> ccdCaseAssignment) {
        List<String> errorList = new ArrayList<>();
        AllocatedBarrister allocatedBarrister = caseData.getAllocatedBarrister();

        Optional<String> userId = organisationService
            .findUserByEmail(allocatedBarrister.getBarristerEmail());

        if (userId.isPresent()) {
            validator.accept(userId.get(), errorList);
        } else {
            errorList.add("Could not find barrister with provided email");
        }

        if (errorList.isEmpty()) {
            userId.ifPresent(ccdCaseAssignment);
        }

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseData.toMap(objectMapper))
            .errors(errorList).build();
    }
}
