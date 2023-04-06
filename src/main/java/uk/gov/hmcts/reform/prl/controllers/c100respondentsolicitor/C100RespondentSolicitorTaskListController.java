package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

@Tag(name = "c100-respondent-task-list-controller")
@Slf4j
@RestController
@RequestMapping("/update-res-task-list")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SecurityRequirement(name = "Bearer Authentication")
public class C100RespondentSolicitorTaskListController extends AbstractCallbackController {

    @PostMapping("/submitted")
    public AboutToStartOrSubmitCallbackResponse handleSubmitted(
        @RequestBody CallbackRequest callbackRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation) {

        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        publishEvent(new CaseDataChanged(caseData));
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
