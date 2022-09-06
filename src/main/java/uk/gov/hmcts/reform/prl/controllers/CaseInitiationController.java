package uk.gov.hmcts.reform.prl.controllers;

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
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessService;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_ROLE;

@Tag(name = "case-initiation-controller")
@RestController
@RequestMapping("/case-initiation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class CaseInitiationController extends AbstractCallbackController {


    private  final AssignCaseAccessService assignCaseAccessService;

    @Autowired
    UserService userService;

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                @RequestBody CallbackRequest callbackRequest) {

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        if (!userService.getUserDetails(authorisation).getRoles().contains(CITIZEN_ROLE)) {
            assignCaseAccessService.assignCaseAccess(caseDetails.getId().toString(), authorisation);
        }

        publishEvent(new CaseDataChanged(caseData));

    }
}
