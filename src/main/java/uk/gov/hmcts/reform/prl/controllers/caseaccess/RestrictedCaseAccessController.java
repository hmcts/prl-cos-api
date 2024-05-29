package uk.gov.hmcts.reform.prl.controllers.caseaccess;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.ccd.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.services.extendedcasedataservice.ExtendedCaseDataService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequestMapping("/case-access")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RestrictedCaseAccessController {
    private final ExtendedCaseDataService caseDataService;
    private final AllTabServiceImpl allTabService;
    private final CcdCoreCaseDataService coreCaseDataService;
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/mark-as-restricted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public void markCaseAsRestricted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws JsonProcessingException {
        log.info("Case details before:: " + objectMapper.writeValueAsString(callbackRequest));

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificEvent(
                String.valueOf(callbackRequest.getCaseDetails().getId()),
                CaseEvent.CHANGE_CASE_ACCESS_AS_SYSUSER.getValue()
            );

        log.info("** markCaseAsRestricted event started");
        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
            startAllTabsUpdateDataContent.startEventResponse(),
            Classification.RESTRICTED
        );
        log.info("** markCaseAsRestricted caseDataContent got Data {} SC {} Reference {}",
                 caseDataContent.getData(), caseDataContent.getSecurityClassification(),
                 caseDataContent.getCaseReference()
        );
        log.info("Response after:: " + objectMapper.writeValueAsString(caseDataContent));

        coreCaseDataService.submitUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataContent,
            String.valueOf(callbackRequest.getCaseDetails().getId()),
            true
        );
        log.info("** markCaseAsRestricted submitUpdate done");
    }

    @PostMapping(path = "/mark-as-private", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as private")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public void markCaseAsPrivate(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws JsonProcessingException {
        log.info("Case details before:: " + objectMapper.writeValueAsString(callbackRequest));

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificEvent(
                String.valueOf(callbackRequest.getCaseDetails().getId()),
                CaseEvent.CHANGE_CASE_ACCESS_AS_SYSUSER.getValue()
            );

        log.info("** markCaseAsPrivate event started");
        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
            startAllTabsUpdateDataContent.startEventResponse(),
            Classification.PRIVATE
        );
        log.info("** markCaseAsPrivate caseDataContent got Data {} SC {} Reference {}",
                 caseDataContent.getData(), caseDataContent.getSecurityClassification(),
                 caseDataContent.getCaseReference()
        );
        log.info("Response after:: " + objectMapper.writeValueAsString(caseDataContent));

        coreCaseDataService.submitUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataContent,
            String.valueOf(callbackRequest.getCaseDetails().getId()),
            true
        );
        log.info("** markCaseAsPrivate submitUpdate done");
    }

    @PostMapping(path = "/mark-as-public", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as public")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public void markCaseAsPublic(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws JsonProcessingException {
        log.info("Case details before:: " + objectMapper.writeValueAsString(callbackRequest));

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificEvent(
                String.valueOf(callbackRequest.getCaseDetails().getId()),
                CaseEvent.CHANGE_CASE_ACCESS_AS_SYSUSER.getValue()
            );

        log.info("** markCaseAsPublic event started");
        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
            startAllTabsUpdateDataContent.startEventResponse(),
            Classification.PUBLIC
        );
        log.info("** markCaseAsPublic caseDataContent got Data {} SC {} Reference {}",
                 caseDataContent.getData(), caseDataContent.getSecurityClassification(),
                 caseDataContent.getCaseReference()
        );
        log.info("Response after:: " + objectMapper.writeValueAsString(caseDataContent));

        coreCaseDataService.submitUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataContent,
            String.valueOf(callbackRequest.getCaseDetails().getId()),
            true
        );
        log.info("** markCaseAsPublic submitUpdate done");
    }

    @PostMapping(path = "/change-case-access", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Change case access")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse changeCaseAccess(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws JsonProcessingException {
        log.info("Case details before:: " + objectMapper.writeValueAsString(callbackRequest));
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("caseDataUpdated::" + caseDataUpdated);
        Map<String, Object> dataClassification
            = caseDataService.getDataClassification(String.valueOf(callbackRequest.getCaseDetails().getId()));
        log.info("dataClassification::" + dataClassification);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .dataClassification(dataClassification)
            .securityClassification("RESTRICTED")
            .build();
        log.info("Response after:: " + objectMapper.writeValueAsString(aboutToStartOrSubmitCallbackResponse));
        return aboutToStartOrSubmitCallbackResponse;
    }
}
