package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.RemoveDraftOrderService;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@SuppressWarnings({"squid:S5665"})
@RestController
@RequiredArgsConstructor
public class RemoveDraftOrderController {

    private final ObjectMapper objectMapper;
    private final RemoveDraftOrderService removeDraftOrderService;
    private final AuthorisationService authorisationService;

    public static final String DRAFT_ORDER_COLLECTION = "draftOrderCollection";
    public static final String REMOVED_DRAFT_ORDER_TEXT = "removeDraftOrderText";
    public static final String CONFIRMATION_HEADER = "# Draft order removed";
    public static final String CONFIRMATION_BODY_FURTHER_DIRECTIONS = """
        ### What happens next \n We will delete this order.""";

    @PostMapping(path = "/populate-remove-draft-order-dropdown", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Populate remove draft order dropdown")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback to populate remove draft order dropdown"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse generateAmendDraftOrderDropDown(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("generateAmendDraftOrderDropDown -->");
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            if (caseData.getDraftOrderCollection() != null
                && !caseData.getDraftOrderCollection().isEmpty()) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(removeDraftOrderService.getDraftOrderDynamicList(
                        caseData,
                        callbackRequest.getEventId(),
                        authorisation
                    )).build();
            } else {
                return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of("There are no draft orders")).build();
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }


    @PostMapping(path = "/remove-draft-order/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to remove draft order")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SubmittedCallbackResponse> handleAmendDraftOrderSubmitted(
        @RequestHeader("Authorization")
        @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {

            CaseDetails caseDetails = callbackRequest.getCaseDetails();

            ResponseEntity<SubmittedCallbackResponse> responseEntity = ResponseEntity
                .ok(SubmittedCallbackResponse.builder()
                        .confirmationHeader(CONFIRMATION_HEADER)
                        .confirmationBody(CONFIRMATION_BODY_FURTHER_DIRECTIONS).build());

            return responseEntity;
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/remove-draft-order/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to remove draft order")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAmendDraftOrderAboutToSubmitted(
        @RequestHeader("Authorization")
        @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {

            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );

            List<Element<DraftOrder>> draftOrderCollection = removeDraftOrderService.removeSelectedDraftOrder(caseData);

            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            caseDataUpdated.put(DRAFT_ORDER_COLLECTION, draftOrderCollection);
            caseDataUpdated.put(REMOVED_DRAFT_ORDER_TEXT, caseData.getRemoveDraftOrderText());

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated).build();

        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
