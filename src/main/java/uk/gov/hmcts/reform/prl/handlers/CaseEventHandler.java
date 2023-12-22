package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.handlers.service.CaseEventHandlerService;
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
    public static final String INTERNAL_UPDATE_TASK_LIST = "internal-update-task-list";
    public static final String ID = "id";

    private final CoreCaseDataService coreCaseDataService;
    private final CaseEventHandlerService caseEventHandlerService;

    @EventListener
    public void handleCaseDataChange(final CaseDataChanged event) {
        final CaseData caseData = event.getCaseData();

        final String taskList = caseEventHandlerService.getUpdatedTaskList(caseData);
        final String respondentTaskListA = caseEventHandlerService.getRespondentTaskList(caseData, C100_RESPONDENT_EVENTS_A);
        final String respondentTaskListB = caseEventHandlerService.getRespondentTaskList(caseData, C100_RESPONDENT_EVENTS_B);
        final String respondentTaskListC = caseEventHandlerService.getRespondentTaskList(caseData, C100_RESPONDENT_EVENTS_C);
        final String respondentTaskListD = caseEventHandlerService.getRespondentTaskList(caseData, C100_RESPONDENT_EVENTS_D);
        final String respondentTaskListE = caseEventHandlerService.getRespondentTaskList(caseData, C100_RESPONDENT_EVENTS_E);

        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            INTERNAL_UPDATE_TASK_LIST,
            Map.of(
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
                String.valueOf(caseData.getId())
            )
        );
    }
}

