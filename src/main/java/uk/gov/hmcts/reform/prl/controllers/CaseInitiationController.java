package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.handlers.CaseEventHandler;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.TaskListRenderer;
import uk.gov.hmcts.reform.prl.services.TaskListService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.JURISDICTION;

@Api
@RestController
@RequestMapping("/case-initiation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInitiationController extends AbstractCallbackController{

    @Autowired
    CoreCaseDataService coreCaseDataService;
    @Autowired
    private final TaskListService taskListService;
    @Autowired
    private final TaskListRenderer taskListRenderer;

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest,
                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {


        final CaseDetails caseDetails = callbackRequest.getCaseDetails();

        final CaseData caseData = getCaseData(caseDetails);


        publishEvent(new CaseDataChanged(caseData, authorisation));
    }
}


