package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;
import uk.gov.hmcts.reform.prl.services.respondent.RespondentTaskListRenderer;
import uk.gov.hmcts.reform.prl.services.respondent.RespondentTaskListService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentCaseEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final RespondentTaskListService respondentTaskListService;
    private final RespondentTaskListRenderer respondentTaskListRenderer;
    private final TaskErrorService taskErrorService;

    @EventListener
    public void handleRespondentCaseDataChange(final CaseDataChanged event) {
        final CaseData caseData = event.getCaseData();

        final String taskList = getRespondentUpdatedTaskList(caseData);

        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-respondent-task-list",
            Map.of("taskList", taskList,"id",String.valueOf(caseData.getId()))

        );
    }

    private String getRespondentUpdatedTaskList(CaseData caseData) {
        final List<Task> tasks = respondentTaskListService.getTasksForRespondentOpenCase(caseData);

        List<EventValidationErrors> eventErrors = taskErrorService.getEventErrors(caseData);

        /* if (caseData.getCaseTypeOfApplication().equalsIgnoreCase(C100_CASE_TYPE)) {
            List<Event> events = respondentTaskListService.getRespondentEvents();
            eventErrors.removeIf(e -> !events.contains(e.getEvent()));
        } */

        return respondentTaskListRenderer
            .render(tasks, eventErrors, caseData);
    }

}


