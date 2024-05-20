package uk.gov.hmcts.reform.prl.controllers.restrictedcaseaccess;

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
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.ccd.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.extendedcasedataservice.ExtendedCaseDataService;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequestMapping("/restricted-case-access")
@RequiredArgsConstructor
public class RestrictedCaseAccessController {
    private final ExtendedCaseDataService caseDataService;
    private final CcdCoreCaseDataService coreCaseDataService;
    private final SystemUserService systemUserService;
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/mark-as-restricted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse markAsRestricted11(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws JsonProcessingException {
        log.info("markAsRestricted7");
        String jsonString = objectMapper.writeValueAsString(callbackRequest);
        log.info("Case details before:: " + objectMapper.writeValueAsString(callbackRequest));
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("caseDataUpdated::" + caseDataUpdated);
        Map<String, Object> dataClassification
            = caseDataService.getDataClassification(String.valueOf(callbackRequest.getCaseDetails().getId()));
        log.info("dataClassification::" + dataClassification);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            //.dataClassification(dataClassification)
            .securityClassification("RESTRICTED")
            .build();
        log.info("Response after:: " + objectMapper.writeValueAsString(aboutToStartOrSubmitCallbackResponse));
        return aboutToStartOrSubmitCallbackResponse;
    }

    @PostMapping(path = "/mark-as-restricted-sys-update", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public void markAsRestrictedAsSysUpdate(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws JsonProcessingException {
        log.info("Case details before:: " + objectMapper.writeValueAsString(callbackRequest));
        String sysAuthorisation = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(sysAuthorisation);
        CaseEvent caseEvent = CaseEvent.UPDATE_ALL_TABS;
        EventRequestData eventRequestData = coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId);
        log.info("** markAsRestrictedAsSysUpdate event starting systemUpdateUserId{} eventRequestData {}",
                 systemUpdateUserId, eventRequestData);
        StartEventResponse startEventResponse =
            coreCaseDataService.startUpdate(
                sysAuthorisation,
                eventRequestData,
                String.valueOf(callbackRequest.getCaseDetails().getId()),
                true
            );
        log.info("** markAsRestrictedAsSysUpdate event started");
        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
            startEventResponse
        );
        log.info("** markAsRestrictedAsSysUpdate caseDataContent got Data {} SC {} Reference {}",
                 caseDataContent.getData(), caseDataContent.getSecurityClassification(),
                 caseDataContent.getCaseReference());
        log.info("Response after:: " + objectMapper.writeValueAsString(caseDataContent));
        coreCaseDataService.submitUpdate(
            sysAuthorisation,
            eventRequestData,
            caseDataContent,
            String.valueOf(callbackRequest.getCaseDetails().getId()),
            true
        );
        log.info("** markAsRestrictedAsSysUpdate submitUpdate done");
    }


    @PostMapping(path = "/mark-as-restricted-sys-update-2", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public void markAsRestrictedAsSysUpdate2(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws JsonProcessingException {
        log.info("Case details before:: " + objectMapper.writeValueAsString(callbackRequest));
        String sysAuthorisation = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(sysAuthorisation);
        CaseEvent caseEvent = CaseEvent.MARK_CASE_AS_RESTRICTED;
        EventRequestData eventRequestData = coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId);
        log.info("** markAsRestrictedAsSysUpdate2 event starting systemUpdateUserId{} eventRequestData {}",
                 systemUpdateUserId, eventRequestData);
        StartEventResponse startEventResponse =
            coreCaseDataService.startUpdate(
                sysAuthorisation,
                eventRequestData,
                String.valueOf(callbackRequest.getCaseDetails().getId()),
                true
            );
        log.info("** markAsRestrictedAsSysUpdate2 event started");
        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification2(
            startEventResponse
        );
        log.info("** markAsRestrictedAsSysUpdate2 caseDataContent got Data {} SC {} Reference {}",
                 caseDataContent.getData(), caseDataContent.getSecurityClassification(),
                 caseDataContent.getCaseReference());
        log.info("Response after:: " + objectMapper.writeValueAsString(caseDataContent));
        coreCaseDataService.submitUpdate(
            sysAuthorisation,
            eventRequestData,
            caseDataContent,
            String.valueOf(callbackRequest.getCaseDetails().getId()),
            true
        );
        log.info("** markAsRestrictedAsSysUpdate2 submitUpdate done");
    }
}

