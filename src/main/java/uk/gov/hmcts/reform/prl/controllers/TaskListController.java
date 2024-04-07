package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.TaskListService;

@Tag(name = "task-list-controller")
@Slf4j
@RestController
@RequestMapping("/update-task-list")
@SecurityRequirement(name = "Bearer Authentication")
public class TaskListController extends AbstractCallbackController {

    private final TaskListService taskListService;

    @Autowired
    public TaskListController(ObjectMapper objectMapper,
                              EventService eventPublisher,
                              TaskListService taskListService) {
        super(objectMapper, eventPublisher);
        this.taskListService = taskListService;
    }

    @PostMapping("/submitted")
    public AboutToStartOrSubmitCallbackResponse handleSubmitted(@RequestBody CallbackRequest callbackRequest,
                                                                @RequestHeader(HttpHeaders.AUTHORIZATION)
                                                                @Parameter(hidden = true) String authorisation) {
        return taskListService.updateTaskList(callbackRequest, authorisation);
    }

    @PostMapping("/updateTaskListOnly")
    public void updateTaskListWhenSubmitted(@RequestBody CallbackRequest callbackRequest,
                                            @RequestHeader(HttpHeaders.AUTHORIZATION)
                                            @Parameter(hidden = true) String authorisation) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        publishEvent(new CaseDataChanged(caseData));
    }
}
