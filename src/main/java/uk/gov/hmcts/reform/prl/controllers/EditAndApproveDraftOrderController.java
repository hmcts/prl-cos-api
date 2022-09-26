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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (caseData.getDraftOrderCollection() != null
            && !caseData.getDraftOrderCollection().isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(draftAnOrderService.getDraftOrderDynamicList(
                    caseData.getDraftOrderCollection())).build();
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
        if (caseData.getDraftOrderCollection() != null
            && !caseData.getDraftOrderCollection().isEmpty()) {
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


    @PostMapping(path = "/judge-populate-draft-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Remove dynamic list from the caseData")
    public AboutToStartOrSubmitCallbackResponse populateJudgeDraftOrder(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(draftAnOrderService.populateSelectedOrderText(
                caseData)).build();

    }

    @PostMapping(path = "/judge-generate-draft-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Remove dynamic list from the caseData")
    public AboutToStartOrSubmitCallbackResponse generateJudgeDraftOrder(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(
            "solicitorOrJudgeDraftOrderDoc",
            draftAnOrderService.generateJudgeDraftOrder(authorisation, caseData)
        );
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated).build();

    }
}
