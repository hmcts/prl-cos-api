package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenEmailService;

@Tag(name = "case-submission-notification-callback-controller")
@RestController
@RequestMapping("/case-submission-notification-callback")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class CaseSubmissionNotificationCallbackController extends AbstractCallbackController {

    private final CitizenEmailService citizenEmailService;

    @PostMapping("/notified")
    public void handleNotification(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
                                @RequestBody CallbackRequest callbackRequest) {

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();

        String caseId = String.valueOf(caseDetails.getId());
        citizenEmailService.sendCitizenCaseSubmissionEmail(authorisation, caseId);
    }
}
