package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;

import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EditAndApproveDraftOrderController {
    private final ObjectMapper objectMapper;
    private final DraftAnOrderService draftAnOrderService;

    @PostMapping(path = "/populate-draft-order-dropdown", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Populate draft order dropdown")
    public AboutToStartOrSubmitCallbackResponse generateDraftOrderDropDown(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        if (caseData.getDraftOrderWithTextCollection() != null
            && !caseData.getDraftOrderWithTextCollection().isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(draftAnOrderService.getDraftOrderDynamicList(
                    caseData.getDraftOrderWithTextCollection())).build();
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of("There are no draft orders")).build();
        }
    }

    @PostMapping(path = "/populate-draft-order-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Populate draft order dropdown")
    public AboutToStartOrSubmitCallbackResponse populateDraftOrderDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        if (caseData.getDraftOrderWithTextCollection() != null
            && !caseData.getDraftOrderWithTextCollection().isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(draftAnOrderService.populateSelectedOrder(
                    caseData)).build();
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of("There are no draft orders")).build();
        }
    }


    @PostMapping(path = "/remove-temp-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Remove dynamic list from the caseData")
    public AboutToStartOrSubmitCallbackResponse removeTempFields(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().remove("draftOrdersDynamicList");
        return AboutToStartOrSubmitCallbackResponse.builder().data(callbackRequest.getCaseDetails().getData()).build();
    }
}
