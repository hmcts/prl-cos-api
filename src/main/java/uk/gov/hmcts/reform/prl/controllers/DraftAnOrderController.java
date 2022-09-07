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
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DraftAnOrderController {

    private final ObjectMapper objectMapper;
    private final DgsService dgsService;
    private final DraftAnOrderService draftAnOrderService;

    @PostMapping(path = "/solicitor-prepopulate-draft-order-text", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Generate text for draft an order")
    public AboutToStartOrSubmitCallbackResponse solicitorDraftAnOrder(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        callbackRequest.getCaseDetails().getData().put(
            "previewDraftAnOrder",
            draftAnOrderService.getTheOrderDraftString(caseData)
        );
        log.info("*** caseDataUpdated {} ***", callbackRequest.getCaseDetails().getData());
        return AboutToStartOrSubmitCallbackResponse.builder().data(callbackRequest.getCaseDetails().getData()).build();
    }

    @PostMapping(path = "/generate-draft-an-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Generate document for draft an order")
    public AboutToStartOrSubmitCallbackResponse draftAnOrderMidEventCallback(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(
            "solicitorDraftOrderDoc",
            draftAnOrderService.generateSolicitorDraftOrder(authorisation, caseData)
        );
        log.info("*** caseDataUpdated {} ***", caseDataUpdated);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
