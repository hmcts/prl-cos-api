package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class FeeAndPayServiceRequestController extends AbstractCallbackController {

    public static final String CONFIRMATION_HEADER_HELP_WITH_FEES = "# Help with fees requested";

    public static final String CONFIRMATION_HEADER = "# Continue to payment";
    private final SolicitorEmailService solicitorEmailService;

    public static final String CONFIRMATION_BODY_PREFIX_HELP_WITH_FEES = "### What happens next \n\n You will receive a confirmation email. "
        + "If the email does not appear in your inbox, check your junk or spam folder."
        + "\n\n The court will review the document and will be in touch to let you know what happens next.";
    public static final String CONFIRMATION_BODY_PREFIX = "### What happens next \n\n The application has been submitted, and you will now need "
        + "to pay the application fee."
        + "\n\n Go to the 'Service request' section to make a payment. Once the fee has been paid, the court will process the application.";
    public static final String CONFIRMATION_BODY_SUFFIX = "/#Service%20Request'>Close and return to case details.</a> \n\n";

    @PostMapping(path = "/payment-confirmation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to create Fee and Pay service request . Returns service request reference if "
        + "successful")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = uk.gov.hmcts.reform.ccd.client.model.CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<SubmittedCallbackResponse> ccdSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        solicitorEmailService.sendAwaitingPaymentEmail(callbackRequest.getCaseDetails());
        log.info("help with fees", callbackRequest.getCaseDetails().getCaseData().getHelpWithFees());
        if (YesOrNo.Yes.equals(callbackRequest.getCaseDetails().getCaseData().getHelpWithFees())) {
            return ok(SubmittedCallbackResponse.builder().confirmationHeader(
                CONFIRMATION_HEADER_HELP_WITH_FEES).confirmationBody(
                CONFIRMATION_BODY_PREFIX_HELP_WITH_FEES + CONFIRMATION_BODY_SUFFIX
            ).build());
        } else {
            return ok(SubmittedCallbackResponse.builder().confirmationHeader(
                CONFIRMATION_HEADER).confirmationBody(
                CONFIRMATION_BODY_PREFIX + CONFIRMATION_BODY_SUFFIX
            ).build());
        }
    }
}
