package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;

@Api
@RestController
@RequestMapping("/update-task-list")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListController extends AbstractCallbackController {

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest,
                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {
        publishEvent(new CaseDataChanged(getCaseData(callbackRequest.getCaseDetails())));
    }
}
