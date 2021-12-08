package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.workflows.CreatePaymentServiceRequestWorkflow;
import uk.gov.hmcts.reform.prl.workflows.ServiceRequestUpdateWorkflow;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class ServiceRequestUpdateCallbackController {

    private final ServiceRequestUpdateWorkflow serviceRequestUpdateWorkflow;
    private final CreatePaymentServiceRequestWorkflow createPaymentServiceRequestWorkflow;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;


    @PostMapping(path = "/service-request-update", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Ways to pay will call this API and send the status of payment with other details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CallbackResponse> serviceRequestUpdate(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws WorkflowException {

        WorkflowResult workflowResult = serviceRequestUpdateWorkflow.run(callbackRequest);
        //CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        return ok(
            CallbackResponse.builder()
                .data(objectMapper.convertValue(workflowResult.getCaseData(), CaseData.class))
                .build()
        );


        //when control comesback, save payment_amount,payment_reference,service_request_status and timestamp
        //if payment status is successfull then update the case state to SUBMITTED_PAID else SUBMITTED_NOT_PAID
    }
}
