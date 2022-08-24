package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeResponseForCitizen;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.FeeService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


@Slf4j
@RequiredArgsConstructor
@Tag(name = "fees-and-payment-controller")
@RestController
@RequestMapping("/fees-and-payment-apis")
public class FeesAndPaymentCitizenController {
    private static final String SERVICE_AUTH = "ServiceAuthorization";

    @Autowired
    private AuthorisationService authorisationService;

    @Autowired
    private FeeService feeService;

    @GetMapping(path = "/getC100ApplicationFees", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Frontend to fetch the Fees Details for C100 Application Submission")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Case is created"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public FeeResponseForCitizen fetchFeesAmount(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTH) String serviceAuthorization
    ) {
        FeeResponse feeResponse = null;
        try {
            if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation)) && Boolean.TRUE.equals(
                authorisationService.authoriseService(serviceAuthorization))) {
                feeResponse = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
            } else {
                throw (new RuntimeException("Invalid Client"));
            }
        } catch (Exception e) {
            return FeeResponseForCitizen.builder()
                .errorRetrievingResponse(e.getMessage())
                .build();
        }
        return FeeResponseForCitizen.builder()
            .amount(feeResponse.getAmount().toString()).build();
    }
}
