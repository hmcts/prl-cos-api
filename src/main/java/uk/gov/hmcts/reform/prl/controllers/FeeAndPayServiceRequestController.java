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
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.solicitoremailnotification.SolicitorEmailNotificationEventEnum;
import uk.gov.hmcts.reform.prl.events.SolicitorNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class FeeAndPayServiceRequestController extends AbstractCallbackController {

    private final AuthorisationService authorisationService;

    private final EventService eventPublisher;

    public static final String CONFIRMATION_HEADER = "# Please visit service request to make the payment";
    public static final String CONFIRMATION_BODY_PREFIX = "### What happens next \n\n The case will now display as 'Pending' in your case list. "
        + "You need to visit Service Request tab to make the payment"
        + "\n\n <a href='/cases/case-details/";
    public static final String CONFIRMATION_BODY_SUFFIX = "/#Service%20Request'>click here to pay</a> \n\n";

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
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            SolicitorNotificationEmailEvent event = prepareAwaitingPaymentEvent(callbackRequest);
            eventPublisher.publishEvent(event);
            return ok(SubmittedCallbackResponse.builder().confirmationHeader(
                CONFIRMATION_HEADER).confirmationBody(
                CONFIRMATION_BODY_PREFIX + callbackRequest.getCaseDetails().getCaseId()
                    + CONFIRMATION_BODY_SUFFIX
            ).build());
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private SolicitorNotificationEmailEvent prepareAwaitingPaymentEvent(CallbackRequest callbackRequest) {
        return SolicitorNotificationEmailEvent.builder()
            .typeOfEvent(SolicitorEmailNotificationEventEnum.awaitingPayment.getDisplayedValue())
            .caseDetails(callbackRequest.getCaseDetails())
            .caseDetailsModel(null)
            .build();
    }
}
