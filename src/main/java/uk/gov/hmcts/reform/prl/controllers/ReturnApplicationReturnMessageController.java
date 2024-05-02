package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.caseworkeremailnotification.CaseWorkerEmailNotificationEventEnum;
import uk.gov.hmcts.reform.prl.events.CaseWorkerNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.handlers.CaseEventHandler;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.ReturnApplicationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@RestController
@Slf4j
public class ReturnApplicationReturnMessageController extends AbstractCallbackController {
    private final UserService userService;
    private final ReturnApplicationService returnApplicationService;
    private final AllTabServiceImpl allTabsService;
    private final AuthorisationService authorisationService;
    private final CaseEventHandler caseEventHandler;

    @Autowired
    public ReturnApplicationReturnMessageController(ObjectMapper objectMapper,
                                                    EventService eventPublisher,
                                                    UserService userService,
                                                    ReturnApplicationService returnApplicationService,
                                                    AllTabServiceImpl allTabsService,
                                                    AuthorisationService authorisationService,
                                                    CaseEventHandler caseEventHandler) {
        super(objectMapper, eventPublisher);
        this.userService = userService;
        this.returnApplicationService = returnApplicationService;
        this.allTabsService = allTabsService;
        this.authorisationService = authorisationService;
        this.caseEventHandler = caseEventHandler;
    }

    @PostMapping(path = "/return-application-return-message", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to get return message of the return application ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback proceeded"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse returnApplicationReturnMessage(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

            CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class)
                .toBuilder()
                .id(callbackRequest.getCaseDetails().getId())
                .build();
            if (!returnApplicationService.noRejectReasonSelected(caseData)) {
                caseDataUpdated.put("returnMessage", returnApplicationService.getReturnMessage(caseData, userDetails));
            }
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/return-application-notification", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to send return application email notification")
    public AboutToStartOrSubmitCallbackResponse returnApplicationEmailNotification(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            CaseWorkerNotificationEmailEvent returnApplicationNotificationEvent = CaseWorkerNotificationEmailEvent.builder()
                .typeOfEvent(CaseWorkerEmailNotificationEventEnum.returnEmailNotification.getDisplayedValue())
                .caseDetailsModel(callbackRequest.getCaseDetails())
                .build();
            eventPublisher.publishEvent(returnApplicationNotificationEvent);
            // Refreshing the page in the same event. Hence no external event call needed.
            // Getting the tab fields and add it to the casedetails..
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            caseData = returnApplicationService.updateMiamPolicyUpgradeDataForConfidentialDocument(caseData, caseDataUpdated);
            caseDataUpdated.put("taskListReturn", returnApplicationService.getReturnMessageForTaskList(caseData));

            String updatedTaskList = caseEventHandler.getUpdatedTaskList(caseData);
            caseDataUpdated.put("taskList", updatedTaskList);

            Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);
            caseDataUpdated.putAll(allTabsFields);

            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
