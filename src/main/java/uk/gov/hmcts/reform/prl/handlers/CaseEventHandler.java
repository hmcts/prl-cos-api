package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.State;
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
import uk.gov.hmcts.reform.prl.services.TaskErrorService;
import uk.gov.hmcts.reform.prl.services.TaskListRenderer;
import uk.gov.hmcts.reform.prl.services.TaskListService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentSolicitorTaskListRenderer;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseEventHandler {

    public static final String C100_RESPONDENT_EVENTS_A = "A";
    public static final String C100_RESPONDENT_EVENTS_B = "B";
    public static final String C100_RESPONDENT_EVENTS_C = "C";
    public static final String C100_RESPONDENT_EVENTS_D = "D";
    public static final String C100_RESPONDENT_EVENTS_E = "E";
    public static final String C100_RESPONDENT_TASK_LIST_A = "respondentTaskListA";
    public static final String C100_RESPONDENT_TASK_LIST_B = "respondentTaskListB";
    public static final String C100_RESPONDENT_TASK_LIST_C = "respondentTaskListC";
    public static final String C100_RESPONDENT_TASK_LIST_D = "respondentTaskListD";
    public static final String C100_RESPONDENT_TASK_LIST_E = "respondentTaskListE";
    public static final String C100_RESPONDENT_TASK_LIST = "respondentTaskList";
    public static final String TASK_LIST = "taskList";
    public static final String ID = "id";

    private final TaskListService taskListService;
    private final TaskListRenderer taskListRenderer;
    private final RespondentSolicitorTaskListRenderer respondentSolicitorTaskListRenderer;
    private final TaskErrorService taskErrorService;
    private final RespondentTaskErrorService respondentTaskErrorService;
    private final AllTabServiceImpl allTabService;

    @EventListener
    public void handleCaseDataChange(final CaseDataChanged event) {
        final String caseId = String.valueOf(event.getCaseData().getId());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
                = allTabService.getStartAllTabsUpdate(caseId);
        final String taskList = getUpdatedTaskList(startAllTabsUpdateDataContent.caseData());
        final String respondentTaskListA = getRespondentTaskList(startAllTabsUpdateDataContent.caseData(), C100_RESPONDENT_EVENTS_A);
        final String respondentTaskListB = getRespondentTaskList(startAllTabsUpdateDataContent.caseData(), C100_RESPONDENT_EVENTS_B);
        final String respondentTaskListC = getRespondentTaskList(startAllTabsUpdateDataContent.caseData(), C100_RESPONDENT_EVENTS_C);
        final String respondentTaskListD = getRespondentTaskList(startAllTabsUpdateDataContent.caseData(), C100_RESPONDENT_EVENTS_D);
        final String respondentTaskListE = getRespondentTaskList(startAllTabsUpdateDataContent.caseData(), C100_RESPONDENT_EVENTS_E);
        Map<String, Object> combinedFieldsMap = Map.of(
                TASK_LIST,
                taskList,
                C100_RESPONDENT_TASK_LIST,
                "",
                C100_RESPONDENT_TASK_LIST_A,
                respondentTaskListA,
                C100_RESPONDENT_TASK_LIST_B,
                respondentTaskListB,
                C100_RESPONDENT_TASK_LIST_C,
                respondentTaskListC,
                C100_RESPONDENT_TASK_LIST_D,
                respondentTaskListD,
                C100_RESPONDENT_TASK_LIST_E,
                respondentTaskListE,
                ID,
                caseId
        );
        allTabService.submitAllTabsUpdate(startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                combinedFieldsMap);
    }

    public String getUpdatedTaskList(CaseData caseData) {
        String taskList = "";
        if (caseData.getState() != null
                && (caseData.getState().equals(State.AWAITING_SUBMISSION_TO_HMCTS)
                || caseData.getState().equals(State.AWAITING_RESUBMISSION_TO_HMCTS))) {
            //taskErrorService.clearErrors();
            final List<Task> tasks = taskListService.getTasksForOpenCase(caseData);
            List<EventValidationErrors> eventErrors = taskErrorService.getEventErrors(caseData);
            if (caseData.getCaseTypeOfApplication().equalsIgnoreCase(C100_CASE_TYPE)) {
                List<Event> events = taskListService.getC100Events(caseData);
                eventErrors.removeIf(e -> !events.contains(e.getEvent()));
            }

            if (caseData.getCaseTypeOfApplication().equalsIgnoreCase(FL401_CASE_TYPE)) {
                List<Event> events = taskListService.getFL401Events(caseData);
                eventErrors.removeIf(e -> !events.contains(e.getEvent()));
            }

            taskList = taskListRenderer
                    .render(
                            tasks,
                            eventErrors,
                            caseData.getCaseTypeOfApplication().equalsIgnoreCase(C100_CASE_TYPE),
                            caseData
                    );
        }
        return taskList;
    }

    public String getRespondentTaskList(CaseData caseData, String respondent) {
        String respondentTaskList = "";
        if (caseData.getRespondents() != null
                && !caseData.getRespondents().isEmpty()) {
            Optional<SolicitorRole> solicitorRole = SolicitorRole.from(respondent);
            if (solicitorRole.isPresent() && caseData.getRespondents().size() > solicitorRole.get().getIndex()) {
                Element<PartyDetails> respondingParty = caseData.getRespondents().get(solicitorRole.get().getIndex());
                if (respondingParty.getValue() != null
                        && respondingParty.getValue().getUser() != null
                        && YesOrNo.Yes.equals(respondingParty.getValue().getUser().getSolicitorRepresented())
                        && respondingParty.getValue().getResponse() != null) {
                    final boolean hasSubmitted = YesOrNo.Yes.equals(respondingParty.getValue().getResponse().getC7ResponseSubmitted());
                    String representedRespondentName = respondingParty.getValue().getLabelForDynamicList();
                    if (hasSubmitted) {
                        return respondentSolicitorTaskListRenderer
                                .render(
                                        null,
                                        null,
                                        respondent,
                                        representedRespondentName,
                                        hasSubmitted,
                                        caseData
                                );
                    } else {
                        respondentTaskErrorService.clearErrors();
                        final List<RespondentTask> tasks = taskListService.getRespondentSolicitorTasks(respondingParty.getValue(),caseData);
                        log.info("Tasks are as : {} ", tasks);
                        List<RespondentEventValidationErrors> eventErrors = respondentTaskErrorService.getEventErrors(caseData);

                        List<RespondentSolicitorEvents> events = taskListService.getRespondentsEvents(caseData);
                        log.info("Events  are as : {} ", events);
                        eventErrors.removeIf(e -> !events.contains(e.getEvent()));
                        return respondentSolicitorTaskListRenderer
                                .render(
                                        tasks,
                                        eventErrors,
                                        respondent,
                                        representedRespondentName,
                                        hasSubmitted,
                                        caseData
                                );
                    }
                }
            }
        }
        return respondentTaskList;
    }
}
