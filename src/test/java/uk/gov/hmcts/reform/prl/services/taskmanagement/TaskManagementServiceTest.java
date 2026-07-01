package uk.gov.hmcts.reform.prl.services.taskmanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.wa.AdditionalProperties;
import uk.gov.hmcts.reform.prl.models.wa.CompletableTaskResponse;
import uk.gov.hmcts.reform.prl.models.wa.TaskData;
import uk.gov.hmcts.reform.prl.services.WaSystemUserService;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskManagementServiceTest {
    @Mock
    private WaSystemUserService waSystemUserService;

    @Mock
    private TaskManagementClient taskManagementClient;

    private static final String HEARING_ID = "100";
    private static final String CASE_ID = "200";
    private static final String EVENT_ID = "eventId";
    private static final String WA_TOKEN = "waToken";

    private static final String CASE_TYPE_ID = "PRLAPPS";

    TaskManagementService taskManagementService;

    @BeforeEach
    void setUp() {
        when(waSystemUserService.getSysUserToken()).thenReturn(WA_TOKEN);

        taskManagementService = new TaskManagementService(waSystemUserService, taskManagementClient);
    }

    @Test
    void shouldFindNoCompletableTasksForCaseHearingEvent() {
        CompletableTaskResponse completableTaskResponse = CompletableTaskResponse.builder()
            .tasks(Collections.emptyList())
            .build();
        when(taskManagementClient.searchForCompletable(anyString(), any())).thenReturn(completableTaskResponse);

        assertTrue(taskManagementService.hasNoCompletableTasksForHearing(HEARING_ID, CASE_ID, EVENT_ID));
    }

    @Test
    void shouldFindCompletableTasksForCaseHearingEvent() {
        CompletableTaskResponse completableTaskResponse = CompletableTaskResponse.builder()
            .tasks(Arrays.asList(TaskData.builder()
                                     .additionalProperties(AdditionalProperties.builder()
                                                               .hearingId(HEARING_ID)
                                                               .build())
                                     .caseTypeId(CASE_TYPE_ID)
                                     .description("someTask")
                                     .taskState("taskState")
                                     .build()))
            .build();
        when(taskManagementClient.searchForCompletable(anyString(), any())).thenReturn(completableTaskResponse);

        assertFalse(taskManagementService.hasNoCompletableTasksForHearing(HEARING_ID, CASE_ID, EVENT_ID));
    }

}
