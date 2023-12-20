package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskListService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskListControllerTest {

    @InjectMocks
    TaskListController taskListController;

    @Mock
    private TaskListService taskListService;

    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;
    CallbackRequest callbackRequest;
    String auth = "authorisation";

    @Before
    public void setup() {
        caseDataMap = new HashMap<>();
        caseData = CaseData.builder()
            .id(12345678L)
            .state(State.SUBMITTED_PAID)
            .build();
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.SUBMITTED_PAID.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    @Test
    public void testHandleSubmitted() {
        when(taskListService.updateTaskList(callbackRequest, auth))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());
        AboutToStartOrSubmitCallbackResponse response = taskListController.handleSubmitted(callbackRequest, auth);

        Assert.assertNotNull(response);
        verify(taskListService, times(1)).updateTaskList(callbackRequest,auth);
    }

    @Test
    public void testUpdateTaskListWhenSubmitted() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        caseData = caseData.toBuilder().dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime)).build();
        CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);
        taskListController.updateTaskListWhenSubmitted(callbackRequest, "testAuth");
        verify(eventPublisher, times(1)).publishEvent(Mockito.any());
    }
}
