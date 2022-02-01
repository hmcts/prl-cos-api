package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.TaskListRenderer;
import uk.gov.hmcts.reform.prl.services.TaskListService;
import uk.gov.hmcts.reform.prl.services.UserService;

@Api
@RestController
@RequestMapping("/case-initiation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInitiationController extends AbstractCallbackController {

    @Autowired
    CoreCaseDataService coreCaseDataService;
    @Autowired
    private final TaskListService taskListService;
    @Autowired
    private final TaskListRenderer taskListRenderer;

    private final UserService userService;

    @PostMapping("/submitted")
    public void handleSubmitted(
        @RequestHeader("Authorization") String authorisation,
        @RequestBody CallbackRequest callbackRequest) {

        UserDetails userDetails = userService.getUserDetails(authorisation);
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final CaseData updatedCaseData = caseData.builder()
            .applicantSolicitorEmailAddress(userDetails.getEmail())
            .caseworkerEmailAddress("prl_caseworker_solicitor@mailinator.com")
            .build();
        publishEvent(new CaseDataChanged(updatedCaseData));
    }
}
