package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DraftAnOrderController {

    @Autowired
    private ObjectMapper objectMapper;

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
            .data(caseData.toBuilder().selectedOrder(caseData.getCreateSelectOrderOptions() != null ? caseData.getCreateSelectOrderOptions().getDisplayedValue() : "").build().toMap(
                CcdObjectMapper.getObjectMapper())).build();

    }

}

