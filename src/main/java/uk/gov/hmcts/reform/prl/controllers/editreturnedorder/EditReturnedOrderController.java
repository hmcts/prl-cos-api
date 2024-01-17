package uk.gov.hmcts.reform.prl.controllers.editreturnedorder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.EditReturnedOrderService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@Slf4j
@RestController
@RequestMapping("/edit-returned-order")
@RequiredArgsConstructor
public class EditReturnedOrderController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ManageOrderService manageOrderService;

    @Autowired
    private DraftAnOrderService draftAnOrderService;

    @Autowired
    EditReturnedOrderService editReturnedOrderService;

    @Autowired
    private AuthorisationService authorisationService;

    @Autowired
    CoreCaseDataService coreCaseDataService;

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
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            if (caseData.getDraftOrderCollection() != null
                && !caseData.getDraftOrderCollection().isEmpty()) {
                DraftOrder selectedOrder = draftAnOrderService.getSelectedDraftOrderDetails(caseData.getDraftOrderCollection(),
                                                                                            caseData.getManageOrders()
                                                                                                .getRejectedOrdersDynamicList());
                Map<String, Object> caseDataUpdated = editReturnedOrderService
                    .populateInstructionsAndDocuments(caseData, authorisation, selectedOrder);
                caseDataUpdated.putAll(draftAnOrderService.populateCommonDraftOrderFields(authorisation, caseData, selectedOrder));
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseDataUpdated).build();
            } else {
                return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of("There are no draft orders")).build();
            }
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
            try {
                log.info("callbackRequest ==>" + objectMapper.writeValueAsString(callbackRequest));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            ManageOrderService.cleanUpSelectedManageOrderOptions(caseDataUpdated);
            coreCaseDataService.triggerEvent(
                JURISDICTION,
                CASE_TYPE,
                callbackRequest.getCaseDetails().getId(),
                "internal-update-all-tabs",
                caseDataUpdated
            );
            return ResponseEntity
                .ok(SubmittedCallbackResponse.builder()
                        .confirmationHeader(CONFIRMATION_HEADER)
                        .confirmationBody(CONFIRMATION_BODY_FURTHER_DIRECTIONS).build());
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
