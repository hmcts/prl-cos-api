package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.RequestUpdateCallbackService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ServiceRequestUpdateCallbackController extends AbstractCallbackController {

    private final String serviceAuth = "ServiceAuthorization";
    private final RequestUpdateCallbackService requestUpdateCallbackService;
    private final AuthTokenGenerator authTokenGenerator;
    private final AuthorisationService authorisationService;

    @Autowired
    ApplicationsTabService applicationsTabService;

    @PostMapping(path = "/service-request-update", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Ways to pay will call this API and send the status of payment with other details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public void serviceRequestUpdate(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(serviceAuth) String serviceAuthorization,
        @RequestBody ServiceRequestUpdateDto serviceRequestUpdateDto
    ) {
        if (authorisationService.authorise(serviceAuthorization)) {
            requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);
        }
    }

    @PostMapping(path = "/bypass-fee-pay", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Ways to pay will call this API and send the status of payment with other details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public void bypassFeeAndPay(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws Exception {
        try {
            log.info("**********************");

            final CaseDetails caseDetails = callbackRequest.getCaseDetails();
            final CaseData caseData = getCaseData(caseDetails);

            log.info("Before application tab service submission");
            applicationsTabService.updateApplicationTabData(caseData);
            log.info("After application tab service");

            PaymentDto paymentDto = PaymentDto.builder()
                .paymentAmount("232")
                .paymentReference("PAY_REF")
                .paymentMethod("PBA")
                .caseReference(String.valueOf(caseData.getId()))
                .accountNumber("111111")
                .build();
            ServiceRequestUpdateDto serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
                .serviceRequestReference(String.valueOf(caseData.getId()))
                .ccdCaseNumber(String.valueOf(caseData.getId()))
                .serviceRequestAmount("232")
                .serviceRequestStatus("Paid")
                .payment(paymentDto)
                .build();

            requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }
}
