package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.State;
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

@Slf4j
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

        if (State.CASE_HEARING.equals(caseData.getState())) {

            final String taskList = getRespondentUpdatedTaskList(caseData);

            coreCaseDataService.triggerEvent(
                JURISDICTION,
                CASE_TYPE,
                caseData.getId(),
                "internal-update-respondent-task-list",
                Map.of("respondentTaskList", taskList)

            );
        }
    }

    private String getRespondentUpdatedTaskList(CaseData caseData) {
        final List<Task> tasks = respondentTaskListService.getTasksForRespondentOpenCase(caseData);

        List<EventValidationErrors> eventErrors = taskErrorService.getEventErrors(caseData);
        log.info("Before rendeting the tasklist..");
        return respondentTaskListRenderer
            .render(tasks, eventErrors, caseData);
    }

}


