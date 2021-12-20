package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;
import uk.gov.hmcts.reform.prl.services.TaskListRenderer;
import uk.gov.hmcts.reform.prl.services.TaskListService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.JURISDICTION;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final TaskListService taskListService;
    private final TaskListRenderer taskListRenderer;
    private final TaskErrorService taskErrorService;

    @EventListener
    public void handleCaseDataChange(final CaseDataChanged event) {
        final CaseData caseData = event.getCaseData();

        final List<Task> tasks = taskListService.getTasksForOpenCase(caseData);

        List<EventValidationErrors> eventErrors = taskErrorService.getEventErrors();

        final String taskList = taskListRenderer.render(tasks, eventErrors);

        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-task-list",
            Map.of("taskList", taskList)

        );
    }
}
//}
