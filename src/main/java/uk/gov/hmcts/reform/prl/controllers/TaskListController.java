package uk.gov.hmcts.reform.prl.controllers;

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
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.TaskListService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBMITTED_STATE;


@Tag(name = "task-list-controller")
@Slf4j
@RestController
@RequestMapping("/update-task-list")
@SecurityRequirement(name = "Bearer Authentication")
public class TaskListController extends AbstractCallbackController {

    private final TaskListService taskListService;
    @Qualifier("allTabsService")
    private final AllTabServiceImpl tabService;
    private final UserService userService;
    private final DocumentGenService dgsService;

    @Autowired
    public TaskListController(ObjectMapper objectMapper,
                              EventService eventPublisher,
                              AllTabServiceImpl tabService,
                              UserService userService,
                              DocumentGenService dgsService
                              TaskListService taskListService) {
        super(objectMapper, eventPublisher);
        this.tabService = tabService;
        this.userService = userService;
        this.dgsService = dgsService;
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
