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
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DraftAnOrderController {

    private final ObjectMapper objectMapper;
    private final DgsService dgsService;
    private final DraftAnOrderService draftAnOrderService;

    @PostMapping(path = "/solicitor-prepopulate-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Generate text for draft an order")
    public AboutToStartOrSubmitCallbackResponse prePopulateFields(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("*** callback req properties before logic {} ***", callbackRequest.getCaseDetails().getData());
        FL404 orderData = FL404.builder()
            .fl404bCaseNumber(String.valueOf(caseData.getId()))
            .fl404bCourtName(caseData.getCourtName())
            .fl404bApplicantName(String.format(PrlAppsConstants.FORMAT, caseData.getApplicantsFL401().getFirstName(),
                                               caseData.getApplicantsFL401().getLastName()
            ))
            .fl404bRespondentName(String.format(PrlAppsConstants.FORMAT, caseData.getRespondentsFL401().getFirstName(),
                                                caseData.getRespondentsFL401().getLastName()
            ))
            .build();

        log.info("FL404b court name: {}", orderData.getFl404bCourtName());

        if (ofNullable(caseData.getRespondentsFL401().getAddress()).isPresent()) {
            orderData = orderData.toBuilder().fl404bRespondentAddress(caseData.getRespondentsFL401()
                                                                          .getAddress()).build();
        }
        if (ofNullable(caseData.getRespondentsFL401().getDateOfBirth()).isPresent()) {
            orderData = orderData.toBuilder().fl404bRespondentDob(caseData.getRespondentsFL401()
                                                                      .getDateOfBirth()).build();
        }
        caseData = caseData.toBuilder().manageOrders(ManageOrders.builder()
                                                         .fl404CustomFields(orderData)
                                                         .build())
            .selectedOrder(caseData.getCreateSelectOrderOptions().getDisplayedValue()).build();
        log.info("*** caseData before sending to text area {} ***", caseData);
        callbackRequest.getCaseDetails().getData().put(
            "previewDraftAnOrder",
            draftAnOrderService.getTheOrderDraftString(caseData)
        );
        callbackRequest.getCaseDetails().getData().putAll(caseData.toMap(objectMapper));
        log.info("*** caseDataUpdated {} ***", callbackRequest.getCaseDetails().getData());
        return AboutToStartOrSubmitCallbackResponse.builder().data(callbackRequest.getCaseDetails().getData()).build();
    }


    @PostMapping(path = "/solicitor-prepopulate-draft-order-text", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Generate text for draft an order")
    public AboutToStartOrSubmitCallbackResponse prepopulateSolicitorDraftAnOrder(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("*** inside prepopulateSolicitorDraftAnOrder() {} ***", caseData);
        callbackRequest.getCaseDetails().getData().put(
            "previewDraftAnOrder",
            draftAnOrderService.getTheOrderDraftString(caseData)
        );
        //callbackRequest.getCaseDetails().getData().putAll(caseData.toMap(objectMapper));
        log.info("*** before returning {} ***", callbackRequest.getCaseDetails().getData());
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
