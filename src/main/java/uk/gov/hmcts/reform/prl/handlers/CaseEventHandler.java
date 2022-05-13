package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.Event;
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

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;


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

        final String taskList = getUpdatedTaskList(caseData);

        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-task-list",
            Map.of("taskList", taskList,"id",String.valueOf(caseData.getId()))

        );
    }

    public String getUpdatedTaskList(CaseData caseData) {
        final List<Task> tasks = taskListService.getTasksForOpenCase(caseData);

        List<EventValidationErrors> eventErrors = taskErrorService.getEventErrors(caseData);

        if (caseData.getCaseTypeOfApplication().equalsIgnoreCase(C100_CASE_TYPE)) {
            List<Event> events = taskListService.getC100Events();
            eventErrors.removeIf(e -> !events.contains(e.getEvent()));
        }

        if (caseData.getCaseTypeOfApplication().equalsIgnoreCase(FL401_CASE_TYPE)) {
            List<Event> events = taskListService.getFL401Events(caseData);
            eventErrors.removeIf(e -> !events.contains(e.getEvent()));
        }

        return taskListRenderer
            .render(tasks, eventErrors, caseData.getCaseTypeOfApplication().equalsIgnoreCase(C100_CASE_TYPE), caseData);

    }
}

