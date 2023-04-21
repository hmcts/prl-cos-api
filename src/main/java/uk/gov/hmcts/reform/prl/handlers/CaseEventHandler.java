package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.c100respondentsolicitor.RespondentEventValidationErrors;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.RespondentTask;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;
import uk.gov.hmcts.reform.prl.services.TaskListRenderer;
import uk.gov.hmcts.reform.prl.services.TaskListService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentSolicitorTaskListRenderer;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final TaskListService taskListService;
    private final TaskListRenderer taskListRenderer;
    private final RespondentSolicitorTaskListRenderer respondentSolicitorTaskListRenderer;
    private final TaskErrorService taskErrorService;
    private final RespondentTaskErrorService respondentTaskErrorService;

    @EventListener
    public void handleCaseDataChange(final CaseDataChanged event) {
        final CaseData caseData = event.getCaseData();

        final String taskList = getUpdatedTaskList(caseData);
        log.info("Generating task list for Respondent A");
        final String respondentTaskListA = getRespondentTaskList(caseData, "A");
        log.info("Generating task list for Respondent B");
        final String respondentTaskListB = getRespondentTaskList(caseData, "B");
        log.info("Generating task list for Respondent C");
        final String respondentTaskListC = getRespondentTaskList(caseData, "C");
        log.info("Generating task list for Respondent D");
        final String respondentTaskListD = getRespondentTaskList(caseData, "D");
        log.info("Generating task list for Respondent E");
        final String respondentTaskListE = getRespondentTaskList(caseData, "E");

        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-task-list",
            Map.of(
                "taskList",
                taskList,
                "respondentTaskList",
                "",
                "respondentTaskListA",
                respondentTaskListA,
                "respondentTaskListB",
                respondentTaskListB,
                "respondentTaskListC",
                respondentTaskListC,
                "respondentTaskListD",
                respondentTaskListD,
                "respondentTaskListE",
                respondentTaskListE,
                "id",
                String.valueOf(caseData.getId())
            )
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

    public String getRespondentTaskList(CaseData caseData, String respondent) {
        String respondentTaskList = "";
        log.info("Generating task list for " + respondent);
        if (caseData.getRespondents() != null
            && !caseData.getRespondents().isEmpty()) {
            Optional<SolicitorRole> solicitorRole = SolicitorRole.from(respondent);
            if (solicitorRole.isPresent() && caseData.getRespondents().size() > solicitorRole.get().getIndex()) {
                Element<PartyDetails> respondingParty = caseData.getRespondents().get(solicitorRole.get().getIndex());
                log.info("Generating task list for " + respondent + " and the index is " + solicitorRole.get().getIndex());
                log.info("finding represented: " + respondingParty.getValue().getUser().getSolicitorRepresented());
                log.info("finding C7 submitted: " + respondingParty.getValue().getResponse().getC7ResponseSubmitted());
                if (respondingParty.getValue() != null
                    && respondingParty.getValue().getUser() != null
                    && YesOrNo.Yes.equals(respondingParty.getValue().getUser().getSolicitorRepresented())
                    && respondingParty.getValue().getResponse() != null
                    && !YesOrNo.Yes.equals(respondingParty.getValue().getResponse().getC7ResponseSubmitted())) {
                    log.info("If logic completed");
                    final List<RespondentTask> tasks = taskListService.getRespondentSolicitorTasks(respondingParty.getValue());
                    log.info("tasks found: " + tasks.size());

                    List<RespondentEventValidationErrors> eventErrors = respondentTaskErrorService.getEventErrors();
                    log.info("eventErrors found: " + eventErrors.size());

                    List<RespondentSolicitorEvents> events = taskListService.getRespondentsEvents();
                    log.info("events found: " + events.size());
                    eventErrors.removeIf(e -> !events.contains(e.getEvent()));
                    log.info("eventErrors found following removal: " + eventErrors.size());

                    String representedRespondentName = respondingParty.getValue().getFirstName().trim() + " "
                        + respondingParty.getValue().getLastName().trim();
                    log.info("representedRespondentName found: " + representedRespondentName);
                    final boolean hasSubmitted = respondingParty.getValue().getResponse() != null
                        && YesOrNo.Yes.equals(respondingParty.getValue().getResponse().getC7ResponseSubmitted());
                    return respondentSolicitorTaskListRenderer
                        .render(
                            tasks,
                            eventErrors,
                            respondent,
                            representedRespondentName,
                            hasSubmitted,
                            caseData.getId()
                        );
                }
            }
        }
        return respondentTaskList;
    }
}

