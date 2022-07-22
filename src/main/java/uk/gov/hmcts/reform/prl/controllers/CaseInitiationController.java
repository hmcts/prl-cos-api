package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessService;

@Api
@RestController
@RequestMapping("/case-initiation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class CaseInitiationController extends AbstractCallbackController {


    private  final AssignCaseAccessService assignCaseAccessService;

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                @RequestBody CallbackRequest callbackRequest) {

        log.info("authorisation token: {}",authorisation);
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        assignCaseAccessService.assignCaseAccess(caseDetails.getId().toString(),authorisation);

        publishEvent(new CaseDataChanged(caseData));

    }
}
