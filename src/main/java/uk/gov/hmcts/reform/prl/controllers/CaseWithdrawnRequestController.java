package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class CaseWithdrawnRequestController extends AbstractCallbackController {

    private final ObjectMapper objectMapper;
    public static final String APPLICATION_WITHDRAWN_SUCCESS_LABEL = "# Application withdrawn";
    public static final String APPLICATION_WITHDRAWN_STATUS_LABEL = "### What happens next \n\n This case will now display as “withdrawn” in "
        + "your case list.";

    public static final String APPLICATION_WITHDRAWN_REQUEST_LABEL = "# Requested Application Withdrawal";
    public static final String APPLICATION_WITHDRAWN_REQUEST_STATUS_LABEL = "### What happens next \n\n The court will consider your "
        + "withdrawal request.";

    public static final String APPLICATION_NOT_WITHDRAWN_REQUEST_LABEL = "# Application not withdrawn";

    @PostMapping(path = "/case-withdrawn-confirmation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
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
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("CaseWithdrawnRequestController CaseData ==> " + caseData);
        WithdrawApplication withDrawApplicationData = caseData.getWithDrawApplicationData();
        Optional<YesOrNo> withdrawApplication = ofNullable(withDrawApplicationData.getWithDrawApplication());
        String caseWithdrawnConfirmationHeader;
        String caseWithdrawnConfirmationBodyPrefix;
        log.info("withdrawApplication ==> " + withdrawApplication);
        log.info("state ==> " + caseData.getState());
        if ((withdrawApplication.isPresent() && Yes.equals(withdrawApplication.get()))) {
            if (State.CASE_ISSUE.equals(caseData.getState())
                || State.AWAITING_RESUBMISSION_TO_HMCTS.equals(caseData.getState()) || State.GATE_KEEPING.equals(caseData.getState())) {
                caseWithdrawnConfirmationHeader = APPLICATION_WITHDRAWN_REQUEST_LABEL;
                caseWithdrawnConfirmationBodyPrefix = APPLICATION_WITHDRAWN_REQUEST_STATUS_LABEL;
            } else {
                caseWithdrawnConfirmationHeader = APPLICATION_WITHDRAWN_SUCCESS_LABEL;
                caseWithdrawnConfirmationBodyPrefix = APPLICATION_WITHDRAWN_STATUS_LABEL;
            }
        } else {
            caseWithdrawnConfirmationHeader = APPLICATION_NOT_WITHDRAWN_REQUEST_LABEL;
            caseWithdrawnConfirmationBodyPrefix = "";
        }
        log.info("caseWithdrawnConfirmationHeader ==> " + caseWithdrawnConfirmationHeader);
        log.info("caseWithdrawnConfirmationBodyPrefix ==> " + caseWithdrawnConfirmationBodyPrefix);

        return ok(SubmittedCallbackResponse.builder().confirmationHeader(
            caseWithdrawnConfirmationHeader).confirmationBody(
            caseWithdrawnConfirmationBodyPrefix
        ).build());
    }
}
