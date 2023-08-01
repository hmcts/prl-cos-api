package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;

@RunWith(SpringRunner.class)
public class TaskListControllerTest {

    @InjectMocks
    TaskListController taskListController;

    @Mock
    EventService eventPublisher;

    @Mock
    AllTabServiceImpl tabService;

    @Mock
    UserService userService;
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

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
    }

    @Test
    public void testHandleSubmitted() {

        CaseDataChanged caseDataChanged = new CaseDataChanged(CaseData.builder().build());
        taskListController.publishEvent(caseDataChanged);

        verify(eventPublisher, times(1)).publishEvent(caseDataChanged);
    }

    @Test
    public void testHandleSubmittedWithoutCourtStaffRoles() {
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().roles(List.of("test role")).build());
        taskListController.handleSubmitted(callbackRequest, "testAuth");
        verify(tabService,times(1)).updateAllTabsIncludingConfTab(Mockito.any(CaseData.class));
    }

    @Test
    public void testHandleSubmittedWithCourtStaffRoles() {
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().roles(ROLES).build());
        taskListController.handleSubmitted(callbackRequest, "testAuth");
        verify(tabService,times(1)).updateAllTabsIncludingConfTab(Mockito.any(CaseData.class));
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
