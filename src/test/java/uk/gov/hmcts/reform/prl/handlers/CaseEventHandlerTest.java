package uk.gov.hmcts.reform.prl.handlers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.TaskListRenderer;
import uk.gov.hmcts.reform.prl.services.TaskListService;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;

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

        final String renderedTaskLists = "<h1>Task 1</h1><h2>Task 2</h2>";

        when(taskListService.getTasksForOpenCase(caseData)).thenReturn(tasks);
        when(taskListRenderer.render(tasks)).thenReturn(renderedTaskLists);

        caseEventHandler.handleCaseDataChange(caseDataChanged);

        verify(taskListService).getTasksForOpenCase(caseData);
        verify(taskListRenderer).render(tasks);

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
