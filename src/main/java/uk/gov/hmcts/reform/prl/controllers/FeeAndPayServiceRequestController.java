package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequiredArgsConstructor
public class FeeAndPayServiceRequestController extends AbstractCallbackController {

    private final PaymentRequestService paymentRequestService;
    private final AuthTokenGenerator authTokenGenerator;
    @Autowired
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/create-payment-service-request", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to create Fee and Pay service request . Returns service request reference if "
        + "successful")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = uk.gov.hmcts.reform.ccd.client.model.CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public CallbackResponse createPaymentServiceRequest(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
        PaymentServiceResponse paymentServiceResponse = paymentRequestService.createServiceRequest(callbackRequest, authorisation);
        CaseData caseData = objectMapper.convertValue(
            CaseData.builder()
                .paymentServiceRequestReferenceNumber(paymentServiceResponse.getServiceRequestReference()).build(),
            CaseData.class
        );
        return CallbackResponse.builder()
                .data(caseData)
                .build();
    }
}
