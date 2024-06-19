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
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.restrictedcaseaccessmanagement.CaseSecurityClassificationEnum;
import uk.gov.hmcts.reform.prl.models.ccd.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.extendedcasedataservice.ExtendedCaseDataService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CHANGE_CASE_ACCESS_AS_SYSUSER;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.MARK_CASE_AS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.MARK_CASE_AS_PUBLIC;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.MARK_CASE_AS_RESTRICTED;

@Slf4j
@RestController
@RequestMapping("/restricted-case-access")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RestrictedCaseAccessController {
    public static final String CASE_SECURITY_CLASSIFICATION = "caseSecurityClassification";
    private final AuthorisationService authorisationService;
    private final ExtendedCaseDataService caseDataService;
    private final AllTabServiceImpl allTabService;
    private final CcdCoreCaseDataService coreCaseDataService;
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse restrictedCaseAccessAboutToSubmit(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
            @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
            @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            log.info("** restrictedCaseAccessAboutToSubmit event started");
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            CaseEvent caseEvent = CaseEvent.fromValue(callbackRequest.getEventId());
            if (MARK_CASE_AS_RESTRICTED.equals(caseEvent)) {
                caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, CaseSecurityClassificationEnum.RESTRICTED.getValue());
            } else if (MARK_CASE_AS_PRIVATE.equals(caseEvent)) {
                caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, CaseSecurityClassificationEnum.PRIVATE.getValue());
            } else if (MARK_CASE_AS_PUBLIC.equals(caseEvent)) {
                caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, CaseSecurityClassificationEnum.PUBLIC.getValue());
            }
            log.info("** restrictedCaseAccessAboutToSubmit abs done");
            return uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse
                    .builder()
                    .data(caseDataUpdated)
                    .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public void restrictedCaseAccessSubmitted(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
            @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
            @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            log.info("** restrictedCaseAccess event started");
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

            log.info("** caseDataUpdated:: " + caseDataUpdated);
            CaseSecurityClassificationEnum caseSecurityClassification
                    = CaseSecurityClassificationEnum.fromValue((String) caseDataUpdated.get(CASE_SECURITY_CLASSIFICATION));
            log.info("CaseSecurityClassificationEnum::" + caseSecurityClassification);
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
                    allTabService.getStartUpdateForSpecificEvent(
                            String.valueOf(callbackRequest.getCaseDetails().getId()),
                            CHANGE_CASE_ACCESS_AS_SYSUSER.getValue()
                    );
            CaseDataContent caseDataContent = null;
            switch (caseSecurityClassification) {
                case RESTRICTED -> {
                    log.info("** inside restriced:: ");
                    caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
                            startAllTabsUpdateDataContent.startEventResponse(),
                            Classification.RESTRICTED
                    );
                }
                case PRIVATE -> {
                    log.info("** inside private:: ");
                    caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
                            startAllTabsUpdateDataContent.startEventResponse(),
                            Classification.PRIVATE
                    );
                }
                case PUBLIC -> {
                    log.info("** inside public:: ");
                    caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
                            startAllTabsUpdateDataContent.startEventResponse(),
                            Classification.PUBLIC
                    );
                }
                default -> {
                    log.info("** inside default:: ");
                    coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
                            startAllTabsUpdateDataContent.startEventResponse(),
                            Classification.PUBLIC
                    );
                }
            }
            coreCaseDataService.submitUpdate(
                    startAllTabsUpdateDataContent.authorisation(),
                    startAllTabsUpdateDataContent.eventRequestData(),
                    caseDataContent,
                    String.valueOf(callbackRequest.getCaseDetails().getId()),
                    true
            );
            log.info("** restrictedCaseAccess submitUpdate done");
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/mark-as-private", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as private")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public void markCaseAsPrivate(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
            @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
            @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
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

            coreCaseDataService.submitUpdate(
                    startAllTabsUpdateDataContent.authorisation(),
                    startAllTabsUpdateDataContent.eventRequestData(),
                    caseDataContent,
                    String.valueOf(callbackRequest.getCaseDetails().getId()),
                    true
            );
            log.info("** markCaseAsPrivate submitUpdate done");
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/mark-as-public", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as public")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public void markCaseAsPublic(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
            @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
            @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
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

            coreCaseDataService.submitUpdate(
                    startAllTabsUpdateDataContent.authorisation(),
                    startAllTabsUpdateDataContent.eventRequestData(),
                    caseDataContent,
                    String.valueOf(callbackRequest.getCaseDetails().getId()),
                    true
            );
            log.info("** markCaseAsPublic submitUpdate done");
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
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
            @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
            @RequestBody CallbackRequest callbackRequest) throws JsonProcessingException {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            log.info("Case details before for changeCaseAccess:: " + objectMapper.writeValueAsString(callbackRequest));
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            log.info("caseDataUpdated::" + caseDataUpdated);
            CaseSecurityClassificationEnum caseSecurityClassification
                    = CaseSecurityClassificationEnum.fromValue((String) caseDataUpdated.get(CASE_SECURITY_CLASSIFICATION));
            log.info("CaseSecurityClassificationEnum::" + caseSecurityClassification);
            Map<String, Object> dataClassification
                    = caseDataService.getDataClassification(String.valueOf(callbackRequest.getCaseDetails().getId()));
            log.info("dataClassification for changeCaseAccess::" + dataClassification);
            AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseDataUpdated)
                    .dataClassification(dataClassification)
                    .securityClassification(String.valueOf(caseSecurityClassification))
                    .build();
            log.info("Response after:: " + objectMapper.writeValueAsString(aboutToStartOrSubmitCallbackResponse));
            return aboutToStartOrSubmitCallbackResponse;
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
