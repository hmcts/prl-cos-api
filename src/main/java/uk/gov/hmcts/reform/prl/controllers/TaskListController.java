package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBMITTED_STATE;


@Api
@Slf4j
@RestController
@RequestMapping("/update-task-list")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListController extends AbstractCallbackController {

    @Autowired
    @Qualifier("allTabsService")
    AllTabServiceImpl tabService;

    @Autowired
    UserService userService;

    @Autowired
    DocumentGenService dgsService;

    @PostMapping("/submitted")
    public AboutToStartOrSubmitCallbackResponse handleSubmitted(@RequestBody CallbackRequest callbackRequest,
                                                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {

        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        publishEvent(new CaseDataChanged(caseData));
        UserDetails userDetails = userService.getUserDetails(authorisation);
        List<String> roles = userDetails.getRoles();
        boolean isCourtStaff = roles.stream().anyMatch(ROLES::contains);
        String state = callbackRequest.getCaseDetails().getState();
        if (isCourtStaff && (SUBMITTED_STATE.equalsIgnoreCase(state) || ISSUED_STATE.equalsIgnoreCase(state))) {
            try {
                log.info("Generating documents for the amended details");
                caseDataUpdated.putAll(dgsService.generateDocuments(authorisation, caseData));
            } catch (Exception e) {
                log.error("Error regenerating the document", e);
            }
        }
        caseData = caseData.toBuilder()
            .c8Document((Document) caseDataUpdated.get("c8Document"))
            .c1ADocument((Document) caseDataUpdated.get("c1ADocument"))
            .c8WelshDocument((Document) caseDataUpdated.get("c8WelshDocument"))
            .finalDocument((Document) caseDataUpdated.get("finalDocument"))
            .finalWelshDocument((Document) caseDataUpdated.get("finalWelshDocument"))
            .c1AWelshDocument((Document) caseDataUpdated.get("c1AWelshDocument"))
            .build();
        tabService.updateAllTabsIncludingConfTab(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
