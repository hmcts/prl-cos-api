package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.payment.CreatePaymentRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeResponseForCitizen;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentStatusResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FETCH_FEE_INVALID_APPLICATION_TYPE;


@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(name = "fees-and-payment-controller")
@RestController
@RequestMapping("/fees-and-payment-apis")
public class FeesAndPaymentCitizenController {
    private static final String SERVICE_AUTH = "ServiceAuthorization";
    private static final String LOGGERMESSAGE = "Invalid Client";

    private final AuthorisationService authorisationService;

    private final FeeService feeService;

    private final PaymentRequestService paymentRequestService;

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
            if (isAuthorized(authorisation, serviceAuthorization)) {
                feeResponse = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
            } else {
                throw (new RuntimeException(LOGGERMESSAGE));
            }
        } catch (Exception e) {
            return FeeResponseForCitizen.builder()
                .errorRetrievingResponse(e.getMessage())
                .build();
        }
        return FeeResponseForCitizen.builder()
            .amount(feeResponse.getAmount().toString()).build();
    }

    @PostMapping(path = "/create-payment", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Endpoint to create payment request . Returns payment related details if "
            + "successful")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment processed.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = uk.gov.hmcts.reform.prl.models.dto.payment.PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public PaymentResponse createPaymentRequest(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(SERVICE_AUTH) String serviceAuthorization,
            @RequestBody CreatePaymentRequest createPaymentRequest
    ) throws Exception {

        if (!isAuthorized(authorization, serviceAuthorization)) {
            throw (new RuntimeException(LOGGERMESSAGE));
        }

        return paymentRequestService.createPayment(authorization, createPaymentRequest);

    }



    @GetMapping(path = "/retrievePaymentStatus/{paymentReference}/{caseId}", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Endpoint to retrieve the payment status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment created"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public PaymentStatusResponse retrievePaymentStatus(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTH) String serviceAuthorization,
        @PathVariable String paymentReference,
        @PathVariable String caseId
    ) throws Exception {
        if (!isAuthorized(authorization, serviceAuthorization)) {
            throw (new RuntimeException(LOGGERMESSAGE));
        }
        log.info("Retrieving payment status for the Case id :{}", caseId);
        return paymentRequestService.fetchPaymentStatus(authorization,paymentReference);



    }


    private boolean isAuthorized(String authorisation, String serviceAuthorization) {
        return Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation)) && Boolean.TRUE.equals(
                authorisationService.authoriseService(serviceAuthorization));
    }

    @PostMapping(path = "/getFeeCode", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Frontend to fetch the Fees code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Fee code fetched"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public FeeResponseForCitizen fetchFeeCode(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTH) String serviceAuthorization,
        @RequestBody FeeRequest feeRequest
    ) {
        FeeResponseForCitizen feeResponseForCitizen = null;
        try {
            if (isAuthorized(authorisation, serviceAuthorization)) {
                feeResponseForCitizen = feeService.fetchFeeCode(feeRequest,authorisation);
            } else {
                throw (new RuntimeException(LOGGERMESSAGE));
            }
        } catch (Exception e) {
            return FeeResponseForCitizen.builder()
                .errorRetrievingResponse(e.getMessage())
                .build();
        }
        return feeResponseForCitizen;
    }

    @GetMapping(path = "/getFee/{applicationType}", produces = APPLICATION_JSON)
    @Operation(description = "API to fetch the application fees by application type")
    public FeeResponseForCitizen fetchFee(@RequestHeader(SERVICE_AUTH) String serviceAuthorization,
                                          @PathVariable String applicationType) {
        if (Boolean.TRUE.equals(authorisationService.authoriseService(serviceAuthorization))) {
            log.info("### Fetch fees for application type: {}", applicationType);
            if (null == applicationType || applicationType.isEmpty()) {
                return FeeResponseForCitizen.builder()
                    .errorRetrievingResponse(FETCH_FEE_INVALID_APPLICATION_TYPE)
                    .build();
            }
            return feeService.fetchFee(applicationType);
        } else {
            throw (new RuntimeException(LOGGERMESSAGE));
        }
    }

}
