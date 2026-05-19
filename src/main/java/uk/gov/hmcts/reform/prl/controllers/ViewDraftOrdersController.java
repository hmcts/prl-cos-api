package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.exception.InvalidClientException;
import uk.gov.hmcts.reform.prl.services.*;

import java.util.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RequestMapping("/draft-orders-list")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ViewDraftOrdersController {

    public static final String READONLY_DRAFT_ORDERS_MESSAGE = "You can only view Draft Orders.";
    private final AuthorisationService authorisationService;
    private final ViewDraftOrdersService viewDraftOrdersService;

    @PostMapping(path = "/populate-draft-orders-list", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse callBackURLAboutToStartEvent(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(Map.of("viewFilteredDraftOrders", viewDraftOrdersService.getDraftOrdersForUser(callbackRequest.getCaseDetails(), authorisation)))
                .build();
        } else {
            throw (new InvalidClientException(INVALID_CLIENT));
        }
    }

    @PostMapping("/populate-draft-orders-list/mid-event")
    public AboutToStartOrSubmitCallbackResponse callBackURLMidEvent() {

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of(READONLY_DRAFT_ORDERS_MESSAGE))
            .build();
    }

    @PostMapping("/populate-draft-orders-list/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse callBackURLAboutToSubmitEvent() {

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of(READONLY_DRAFT_ORDERS_MESSAGE))
            .build();
    }

    @PostMapping("/populate-draft-orders-list/submitted")
    public AboutToStartOrSubmitCallbackResponse callBackURLSubmittedEvent() {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of(READONLY_DRAFT_ORDERS_MESSAGE))
            .build();
    }
}
