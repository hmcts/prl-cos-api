package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Mock
    private ObjectMapper objectMapper;

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
    public void handleSubmitted() throws JsonProcessingException {
        when(taskListService.updateTaskList(callbackRequest, auth))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());
        Assert.assertNotNull(taskListController.handleSubmitted(callbackRequest, auth));;
    }

    //    @Test
    //    public void testHandleSubmittedWithoutCourtStaffRoles() throws JsonProcessingException {
    //        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().roles(List.of("test role")).build());
    //        taskListController.handleSubmitted(callbackRequest,"testAuth");
    //        verify(tabService,times(1)).updateAllTabsIncludingConfTab(Mockito.any(CaseData.class));
    //    }
    //
    //    @Test
    //    public void testHandleSubmittedWithCourtStaffRoles() throws JsonProcessingException {
    //        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().roles(ROLES).build());
    //        taskListController.handleSubmitted(callbackRequest,"testAuth");
    //        verify(tabService,times(1)).updateAllTabsIncludingConfTab(Mockito.any(CaseData.class));
    //    }
    //
    //    @Test
    //    public void testHandleSubmittedForGateKeepingState() throws Exception {
    //        Map<String, Object> documentMap = new HashMap<>();
    //        documentMap.put("c1ADocument", Document.builder().build());
    //        documentMap.put("c8Document", Document.builder().build());
    //        documentMap.put("C8WelshDocument", Document.builder().build());
    //        documentMap.put("finalDocument", Document.builder().build());
    //        documentMap.put("finalWelshDocument", Document.builder().build());
    //        documentMap.put("c1AWelshDocument", Document.builder().build());
    //        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
    //        when(dgsService.generateDocuments("testAuth",caseData)).thenReturn(documentMap);
    //        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().roles(ROLES).build());
    //        taskListController.handleSubmitted(callbackRequest,"testAuth");
    //        verify(tabService,times(1)).updateAllTabsIncludingConfTab(Mockito.any(CaseData.class));
    //    }
    //
    //    @Test
    //    public void testUpdateTaskListWhenSubmitted() {
    //        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
    //        caseData = caseData.toBuilder().dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime)).build();
    //        CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);
    //        taskListController.updateTaskListWhenSubmitted(callbackRequest, "testAuth");
    //        verify(eventPublisher, times(1)).publishEvent(Mockito.any());
    //    }
}
