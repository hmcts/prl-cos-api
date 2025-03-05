package uk.gov.hmcts.reform.prl.controllers.bulkscan;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.*;
import static uk.gov.hmcts.reform.prl.enums.State.SUBMITTED_PAID;
import static uk.gov.hmcts.reform.prl.services.citizen.CitizenCaseUpdateService.CASE_STATUS;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BulkScanController {
    private final AllTabServiceImpl allTabsService;
    private final PaymentRequestService paymentRequestService;
    private final AuthorisationService authorisationService;


    @PostMapping(path = "/bulkscan-case-submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to refresh the tabs")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse bulkScanCaseSubmission(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            caseDataUpdated.put("id", callbackRequest.getCaseDetails().getId());
            callbackRequest.setCaseDetails(callbackRequest.getCaseDetails().toBuilder().data(caseDataUpdated).build());
            PaymentServiceResponse paymentServiceResponse = paymentRequestService.createServiceRequestFromCcdCallack(
                callbackRequest,
                authorisation
            );
            log.info("Payment service response: {}", paymentServiceResponse);
            log.info("Payment service request reference number: {}", paymentServiceResponse.getServiceRequestReference());
            caseDataUpdated.put("paymentServiceRequestReferenceNumber", paymentServiceResponse.getServiceRequestReference());
            allTabsService.updateAllTabsIncludingConfTab(String.valueOf(callbackRequest.getCaseDetails().getId()));
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/bulkscan/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "about to submit callback for Process Urgent Help with Fees event.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse bulkScanHandleSubmitted(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            caseDataUpdated.put(CASE_STATUS, CaseStatus.builder()
                .state(SUBMITTED_PAID.getLabel())
                .build());
            caseDataUpdated.put(DATE_SUBMITTED_FIELD, DateTimeFormatter.ISO_LOCAL_DATE
                .format(ZonedDateTime.now(ZoneId.of("Europe/London"))));
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
