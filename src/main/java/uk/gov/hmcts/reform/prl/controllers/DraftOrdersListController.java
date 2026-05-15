package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.exception.InvalidClientException;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RequestMapping("/draft-orders-list")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DraftOrdersListController {
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final AuthorisationService authorisationService;

    @PostMapping(path = "/populate-draft-orders-list", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateFl404Fields(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestHeader(value = PrlAppsConstants.CLIENT_CONTEXT_HEADER_PARAMETER, required = false) String clientContext,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            List<Element<DraftOrder>> viewFilteredDraftOrders = caseData.getDraftOrderCollection().stream()
                .filter(e -> Objects.equals(userDetails.getEmail(), e.getValue().getOtherDetails().getOrderCreatedByEmailId()))
                .toList();

            Map<String, Object> response = Map.of("viewFilteredDraftOrders", viewFilteredDraftOrders);
            return AboutToStartOrSubmitCallbackResponse.builder().data(response).build();
        } else {
            throw (new InvalidClientException(INVALID_CLIENT));
        }
    }
}
