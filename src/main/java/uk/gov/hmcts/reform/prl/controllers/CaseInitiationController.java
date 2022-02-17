package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@Api
@RestController
@RequestMapping("/case-initiation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInitiationController extends AbstractCallbackController {

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        publishEvent(new CaseDataChanged(caseData));

    }
}
