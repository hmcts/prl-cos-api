package uk.gov.hmcts.reform.prl.controllers.editreturnedorder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EditReturnedOrderService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@SuppressWarnings({"squid:S5665"})
@RequestMapping("/edit-returned-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EditReturnedOrderController {
    private final EditReturnedOrderService editReturnedOrderService;
    private final AuthorisationService authorisationService;
    private final AllTabServiceImpl allTabService;

    private static final String CONFIRMATION_HEADER = "# Draft order resubmitted";
    private static final String CONFIRMATION_BODY_FURTHER_DIRECTIONS = """
        ### What happens next \n The judge will review the edits you have made to this order.
        """;

    @PostMapping(path = "/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate dynamic list for returned orders")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return editReturnedOrderService.handleAboutToStartCallback(authorisation, callbackRequest);
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/mid-event/populate-instructions", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate instructions to solicitor")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse populateInstructionsToSolicitor(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return editReturnedOrderService.populateInstructionsAndFieldsForLegalRep(authorisation, callbackRequest, null);
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to display confirmation of event submission")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SubmittedCallbackResponse> handleEditAndReturnedSubmitted(
        @RequestHeader("Authorization")
        @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {

            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartAllTabsUpdate(String.valueOf(
                callbackRequest.getCaseDetails().getId()));
            Map<String, Object> caseDataUpdated = startAllTabsUpdateDataContent.caseDataMap();
            ManageOrderService.cleanUpSelectedManageOrderOptions(caseDataUpdated);
            allTabService.submitAllTabsUpdate(
                startAllTabsUpdateDataContent.authorisation(),
                String.valueOf(callbackRequest.getCaseDetails().getId()),
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                caseDataUpdated);
            return ResponseEntity
                .ok(SubmittedCallbackResponse.builder()
                        .confirmationHeader(CONFIRMATION_HEADER)
                        .confirmationBody(CONFIRMATION_BODY_FURTHER_DIRECTIONS).build());
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
