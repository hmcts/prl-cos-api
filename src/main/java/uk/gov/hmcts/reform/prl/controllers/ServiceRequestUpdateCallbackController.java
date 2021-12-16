package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.RequestUpdateCallbackService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ServiceRequestUpdateCallbackController {

    private final String serviceAuth = "ServiceAuthorization";
    private final RequestUpdateCallbackService requestUpdateCallbackService;
    private final AuthTokenGenerator authTokenGenerator;



    @PostMapping(path = "/service-request-update", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Ways to pay will call this API and send the status of payment with other details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public void serviceRequestUpdate(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(serviceAuth) String serviceAuthorization,
        @RequestBody ServiceRequestUpdateDto serviceRequestUpdateDto
    ) throws Exception {
        try {
            requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);
        } catch (Exception ex) {
            log.error(
                "Payment callback is unsuccessful for the CaseID: {}",
                serviceRequestUpdateDto.getCcdCaseNumber()
            );
            throw new Exception(ex.getMessage());
        }
    }
}
