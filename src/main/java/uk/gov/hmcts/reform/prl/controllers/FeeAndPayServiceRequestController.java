package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.solicitoremailnotification.SolicitorEmailNotificationEventEnum;
import uk.gov.hmcts.reform.prl.events.SolicitorNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.FeeAndPayServiceRequestService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;

import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class FeeAndPayServiceRequestController extends AbstractCallbackController {
    public static final String CONFIRMATION_HEADER_HELP_WITH_FEES = "# Help with fees requested";
    public static final String CONFIRMATION_HEADER = "# Continue to payment";
    public static final String SERVICE_REQUEST_TAB = "#Service%20Request";
    public static final String CONFIRMATION_BODY_PREFIX_HELP_WITH_FEES = """
        ### What happens next

        You will receive a confirmation email.
        If the email does not appear in your inbox, check your junk or spam folder.

        The court will review your help with fees application and tell you what happens next.""";
    public static final String HWF_NO_EMAIL_CONTENT = """
        ### What happens next


        The case will now display as Pending in your case list. You need to visit Service Request tab to make the payment.

        """;
    public static final String PAY_CONTENT = "\">Pay the application fee.</a>";
    public static final String CASE_DETAILS_URL = "/cases/case-details/";

    private final SolicitorEmailService solicitorEmailService;
    private final FeeAndPayServiceRequestService feeAndPayServiceRequestService;
    private final AuthorisationService authorisationService;
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    @Autowired
    protected FeeAndPayServiceRequestController(ObjectMapper objectMapper,
                                                EventService eventPublisher,
                                                SolicitorEmailService solicitorEmailService,
                                                FeeAndPayServiceRequestService feeAndPayServiceRequestService,
                                                AuthorisationService authorisationService,
                                                PartyLevelCaseFlagsService partyLevelCaseFlagsService) {
        super(objectMapper, eventPublisher);
        this.solicitorEmailService = solicitorEmailService;
        this.feeAndPayServiceRequestService = feeAndPayServiceRequestService;
        this.authorisationService = authorisationService;
        this.partyLevelCaseFlagsService = partyLevelCaseFlagsService;
    }

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
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            if (YesOrNo.Yes.equals(callbackRequest.getCaseDetails().getCaseData().getHelpWithFees())) {
                solicitorEmailService.sendHelpWithFeesEmail(callbackRequest.getCaseDetails());
                return ok(SubmittedCallbackResponse
                              .builder()
                              .confirmationHeader(CONFIRMATION_HEADER_HELP_WITH_FEES)
                              .confirmationBody(CONFIRMATION_BODY_PREFIX_HELP_WITH_FEES)
                              .build());
            } else {
                eventPublisher.publishEvent(prepareAwaitingPaymentEvent(callbackRequest));
                if (CaseCreatedBy.COURT_ADMIN.equals(callbackRequest.getCaseDetails().getCaseData().getCaseCreatedBy())) {
                    partyLevelCaseFlagsService.generateAndStoreCaseFlags(callbackRequest.getCaseDetails().getCaseId());
                }
                return ok(SubmittedCallbackResponse
                              .builder()
                              .confirmationHeader(CONFIRMATION_HEADER)
                              .confirmationBody(HWF_NO_EMAIL_CONTENT
                                                    + "<a href=\"" + CASE_DETAILS_URL
                                                    + callbackRequest.getCaseDetails().getCaseId()
                                                    + SERVICE_REQUEST_TAB + PAY_CONTENT)
                              .build());
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
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
        List<String> errorList =
            feeAndPayServiceRequestService.validateSuppressedHelpWithFeesCheck(callbackRequest);
        return CallbackResponse.builder()
            .errors(errorList)
            .build();
    }

    private SolicitorNotificationEmailEvent prepareAwaitingPaymentEvent(CallbackRequest callbackRequest) {
        return SolicitorNotificationEmailEvent.builder()
            .typeOfEvent(SolicitorEmailNotificationEventEnum.awaitingPayment.getDisplayedValue())
            .caseDetails(callbackRequest.getCaseDetails())
            .caseDetailsModel(null)
            .build();
    }
}
