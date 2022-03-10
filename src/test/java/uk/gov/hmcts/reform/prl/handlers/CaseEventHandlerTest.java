package uk.gov.hmcts.reform.prl.handlers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;
import uk.gov.hmcts.reform.prl.services.TaskListRenderer;
import uk.gov.hmcts.reform.prl.services.TaskListService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.FINISHED;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.NOT_STARTED;


@RunWith(MockitoJUnitRunner.class)
public class CaseEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private TaskListService taskListService;

    @Mock
    private TaskListRenderer taskListRenderer;

    @Mock
    private TaskErrorService taskErrorService;

    @InjectMocks
    private CaseEventHandler caseEventHandler;

    @Test
    public void shouldUpdateTaskListForCasesInOpenStateC100() {
        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        final CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);

        final List<Event> c100Events = List.of(
            CASE_NAME,
            MIAM
        );

        List<EventValidationErrors> errors = new ArrayList<>();

        EventValidationErrors error1 = EventValidationErrors.builder()
            .event(FL401_TYPE_OF_APPLICATION)
            .build();

        EventValidationErrors error2 = EventValidationErrors.builder()
            .event(ALLEGATIONS_OF_HARM)
            .build();

        errors.add(error1);
        errors.add(error2);

        when(taskErrorService.getEventErrors(caseData)).thenReturn(errors);

        final List<Task> c100Tasks = List.of(
            Task.builder().event(CASE_NAME).state(FINISHED).build(),
            Task.builder().event(MIAM).state(NOT_STARTED).build());

        final String c100renderedTaskList = "<h1>Case Name</h1><h2>Miam</h2>";

        when(taskListService.getTasksForOpenCase(caseData)).thenReturn(c100Tasks);
        when(taskListRenderer.render(c100Tasks, errors, true, caseData)).thenReturn(c100renderedTaskList);

        caseEventHandler.handleCaseDataChange(caseDataChanged);

        verify(taskListService).getTasksForOpenCase(caseData);
        verify(taskListRenderer).render(c100Tasks, errors, true, caseData);
        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-task-list",
            Map.of("taskList", c100renderedTaskList,"id",String.valueOf(caseData.getId()))
        );
    }

    @Test
    public void shouldUpdateTaskListForCasesInOpenStateFl401() {
        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();
        final CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);

        final List<Event> fl410Events = List.of(
            CASE_NAME,
            FL401_TYPE_OF_APPLICATION
        );


        final List<Task> fl401Tasks = List.of(
            Task.builder().event(CASE_NAME).state(FINISHED).build(),
            Task.builder().event(FL401_TYPE_OF_APPLICATION).state(NOT_STARTED).build());

        final String fl410renderedTaskList = "<h1>Case Name</h1><h2>Type of Application</h2>";

        final List<EventValidationErrors> eventsErrors = Collections.emptyList();

        when(taskListService.getTasksForOpenCase(caseData)).thenReturn(fl401Tasks);
        when(taskListRenderer.render(fl401Tasks, eventsErrors, false, caseData)).thenReturn(fl410renderedTaskList);

        caseEventHandler.handleCaseDataChange(caseDataChanged);

        verify(taskListService).getTasksForOpenCase(caseData);
        verify(taskListRenderer).render(fl401Tasks, eventsErrors, false, caseData);

        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-task-list",
            Map.of("taskList", fl410renderedTaskList,"id",String.valueOf(caseData.getId()))
        );
    }
}
