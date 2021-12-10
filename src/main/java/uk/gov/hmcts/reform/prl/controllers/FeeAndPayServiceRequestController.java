package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class FeeAndPayServiceRequestController extends AbstractCallbackController {

    private final PaymentRequestService paymentRequestService;
    private final AuthTokenGenerator authTokenGenerator;



    @PostMapping(path = "/create-payment-service-request", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to create Fee and Pay service request . Returns service request reference if " +
        "successful")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CallbackResponse> createPaymentServiceRequest(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        PaymentServiceResponse paymentServiceResponse = paymentRequestService.createServiceRequest(callbackRequest, authorisation);
        callbackRequest.getCaseDetails().getData().put(
            "paymentServiceRequestReferenceNumber",
            paymentServiceResponse.getServiceRequestReference()
        );
        return ok(
            CallbackResponse.builder()
                .data(getCaseData(callbackRequest.getCaseDetails()))
                .build()
        );
    }
}
