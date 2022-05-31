package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api
@RestController
@Slf4j
public class ResetAccessCodeController {

    @Autowired
    private CaseInviteManager caseInviteManager;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(path = "/regenerate-access-code", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to regenerate and send access code")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Resubmission completed"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse resetPin(
        @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = new HashMap<>(callbackRequest.getCaseDetails().getData());
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("Regenerating access code for case {}", caseData.getId());
        caseData = caseInviteManager.reGeneratePinAndSendNotificationEmail(caseData);
        caseDataUpdated.put("respondentCaseInvites",caseData.getRespondentCaseInvites());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }
}
