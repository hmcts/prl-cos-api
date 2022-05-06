package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

@Api
@RestController
@RequestMapping("/case-invite")
public class CaseInviteController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    C100CaseInviteService c100CaseInviteService;

    @PostMapping("/about-to-submit")
    public CallbackResponse generateCaseInvites(@RequestBody CallbackRequest callbackRequest) {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseData = c100CaseInviteService.generateCaseInviteForAllRespondentWithEmailPresent(caseData);

        return CallbackResponse.builder()
            .data(caseData)
            .build();

    }

    @PostMapping("/submitted")
    public void sendCaseInvites(@RequestBody CallbackRequest callbackRequest) {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);


    }

}
