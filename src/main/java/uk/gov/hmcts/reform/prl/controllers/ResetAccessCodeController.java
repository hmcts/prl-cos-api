package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ResetAccessCodeController extends AbstractCallbackController {
    private final CaseInviteManager caseInviteManager;
    private final ObjectMapper objectMapper;
    private final AuthorisationService authorisationService;

    @PostMapping(path = "/regenerate-access-code", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resubmission completed"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse resetAccessCode(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Map<String, Object> caseDataUpdated = new HashMap<>(callbackRequest.getCaseDetails().getData());
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            log.info("Regenerating access code for case {}", caseData.getId());
            caseData = caseInviteManager.reGeneratePinAndSendNotificationEmail(caseData);
            caseDataUpdated.put("caseInvites", caseData.getCaseInvites());
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated)
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
