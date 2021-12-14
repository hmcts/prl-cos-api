package uk.gov.hmcts.reform.prl.handlers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.TaskListRenderer;
import uk.gov.hmcts.reform.prl.services.TaskListService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ALLEGATIONS_OF_HARM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ATTENDING_THE_HEARING_ERROR;

@RunWith(MockitoJUnitRunner.class)
public class CaseEventHandlerTest {

    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    TaskListService taskListService;
    @Mock
    TaskListRenderer taskListRenderer;

    @InjectMocks
    CaseEventHandler caseEventHandler;

    @Test
    public void handleCaseDataChangeTest() {

        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .build();

        CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);

        final List<Task> tasks = List.of(
            Task.builder().event(CASE_NAME).build(),
            Task.builder().event(ALLEGATIONS_OF_HARM).build());

        final List<EventValidationErrors> errors = List.of(
            EventValidationErrors.builder().event(ALLEGATIONS_OF_HARM)
                .errors(Collections.singletonList(ALLEGATIONS_OF_HARM_ERROR.toString())).build(),
            EventValidationErrors.builder().event(ATTENDING_THE_HEARING)
                .errors(Collections.singletonList(ATTENDING_THE_HEARING_ERROR.toString())).build()
        );

        final String renderedTaskLists = "<h1>Task 1</h1><h2>Task 2</h2>";

        when(taskListService.getTasksForOpenCase(caseData)).thenReturn(tasks);
        when(taskListRenderer.render(tasks, errors)).thenReturn(renderedTaskLists);

        caseEventHandler.handleCaseDataChange(caseDataChanged);

        verify(taskListService).getTasksForOpenCase(caseData);
        verify(taskListRenderer).render(tasks, errors);

        String jurisdiction = "PRIVATELAW";
        String caseType = "C100";

        verify(coreCaseDataService).triggerEvent(
            jurisdiction,
            caseType,
            caseData.getId(),
            "internal-update-task-list",
            Map.of("taskList", renderedTaskLists)
        );

    }

}
