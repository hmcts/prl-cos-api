package uk.gov.hmcts.reform.prl.handlers.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
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

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseEventHandlerService {
    private final TaskListService taskListService;
    private final TaskListRenderer taskListRenderer;
    private final RespondentSolicitorTaskListRenderer respondentSolicitorTaskListRenderer;
    private final TaskErrorService taskErrorService;
    private final RespondentTaskErrorService respondentTaskErrorService;

    public String getUpdatedTaskList(CaseData caseData) {
        String taskList = "";
        if (caseData.getState() != null
            && (caseData.getState().equals(State.AWAITING_SUBMISSION_TO_HMCTS)
            || caseData.getState().equals(State.AWAITING_RESUBMISSION_TO_HMCTS))) {
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
                                caseData.getId()
                            );
                    } else {
                        final List<RespondentTask> tasks = taskListService.getRespondentSolicitorTasks(respondingParty.getValue());

                        List<RespondentEventValidationErrors> eventErrors = respondentTaskErrorService.getEventErrors();

                        List<RespondentSolicitorEvents> events = taskListService.getRespondentsEvents();
                        eventErrors.removeIf(e -> !events.contains(e.getEvent()));
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
        }
        return respondentTaskList;
    }
}

