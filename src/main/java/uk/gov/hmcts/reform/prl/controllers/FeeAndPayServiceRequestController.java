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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.services.FeeAndPayServiceRequestService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class FeeAndPayServiceRequestController extends AbstractCallbackController {

    public static final String CONFIRMATION_HEADER_HELP_WITH_FEES = "# Help with fees requested";

    public static final String CONFIRMATION_HEADER = "# Continue to payment";
    public static final String XUI_CASE_PATH = "/cases/case-details/";
    public static final String SERVICE_REQUEST_TAB = "#Service%20Request";
    private final SolicitorEmailService solicitorEmailService;
    public static final String CONFIRMATION_BODY_PREFIX_HELP_WITH_FEES = "### What happens next \n\n You will receive a confirmation email. "
        + "If the email does not appear in your inbox, check your junk or spam folder."
        + "\n\n The court will review your help with fees application and tell you what happens next.";

    private final FeeAndPayServiceRequestService feeAndPayServiceRequestService;

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
        if (YesOrNo.Yes.equals(callbackRequest.getCaseDetails().getCaseData().getHelpWithFees())) {
            solicitorEmailService.sendHelpWithFeesEmail(callbackRequest.getCaseDetails());

            return ok(SubmittedCallbackResponse.builder().confirmationHeader(
                CONFIRMATION_HEADER_HELP_WITH_FEES).confirmationBody(
                CONFIRMATION_BODY_PREFIX_HELP_WITH_FEES
            ).build());
        } else {
            solicitorEmailService.sendAwaitingPaymentEmail(callbackRequest.getCaseDetails());
            String serviceRequestUrl = XUI_CASE_PATH + callbackRequest.getCaseDetails().getCaseId() + SERVICE_REQUEST_TAB;
            String confirmationBodyPrefix = "### What happens next \n\n The case will now display as Pending in your case list. "
                + "You need to visit Service Request tab to make the payment. \n\n" + "<a href=\"" + serviceRequestUrl + "\">Pay the application fee.</a>";

            return ok(SubmittedCallbackResponse.builder().confirmationHeader(
                CONFIRMATION_HEADER).confirmationBody(
                confirmationBodyPrefix
            ).build());
        }
    }

    @PostMapping(path = "/validate-help-with-fees", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback validate help with fees number .")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = uk.gov.hmcts.reform.ccd.client.model.CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public CallbackResponse helpWithFeesValidator(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        List<String> errorList = new ArrayList<>();

        if (feeAndPayServiceRequestService.validateHelpWithFeesNumber(callbackRequest)) {
            errorList.add("The help with fees number is incorrect");
        }

        return CallbackResponse.builder()
            .errors(errorList)
            .build();
    }
}
