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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DraftAnOrderController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ManageOrderService manageOrderService;

    @PostMapping(path = "/selected-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateHeader(
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toBuilder()
                      .selectedOrder(caseData.getCreateSelectOrderOptions() != null
                                         ? caseData.getCreateSelectOrderOptions().getDisplayedValue() : "")
                      .dateOrderMade(LocalDate.now())
                      .build().toMap(CcdObjectMapper.getObjectMapper())).build();

    }

    @PostMapping(path = "/populate-draft-order-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateFl404Fields(
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("Court name before prepopulate: {}", caseData.getCourtName());
            caseData = manageOrderService.populateCustomOrderFields(caseData);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(CcdObjectMapper.getObjectMapper())).build();
    }

    @PostMapping(path = "/generate-doc", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse generateDoc(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("**********callbackRequest**********{}", callbackRequest);
        if (callbackRequest
            .getCaseDetailsBefore() != null && callbackRequest
            .getCaseDetailsBefore().getData().get(COURT_NAME) != null) {
            caseData.setCourtName(callbackRequest
                                      .getCaseDetailsBefore().getData().get(COURT_NAME).toString());
        }
        log.info("Case data before adding custom fields {}", caseData);
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
            orderData = orderData.toBuilder().fl404bRespondentAddress(caseData.getRespondentsFL401().getAddress()).build();
        }
        if (ofNullable(caseData.getRespondentsFL401().getDateOfBirth()).isPresent()) {
            orderData = orderData.toBuilder().fl404bRespondentDob(caseData.getRespondentsFL401().getDateOfBirth()).build();
        }
        caseData = caseData.toBuilder().manageOrders(ManageOrders.builder()
                                                         .fl404CustomFields(orderData)
                                                         .build()).build();
        log.info("Case data before generating doc: {}", caseData);
        caseDataUpdated = manageOrderService.getCaseData(authorisation, caseData);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


}

