package uk.gov.hmcts.reform.prl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.c100respondentsolicitor.RespondentEventValidationErrors;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.RespondentTask;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;
import uk.gov.hmcts.reform.prl.services.TaskListRenderer;
import uk.gov.hmcts.reform.prl.services.TaskListService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentSolicitorTaskListRenderer;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_BEHAVIOUR;
import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.FL401_TYPE_OF_APPLICATION_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.RESPONDENT_BEHAVIOUR_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.TYPE_OF_APPLICATION_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONSENT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.FINISHED;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.NOT_STARTED;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseEventHandlerTest {

    @Mock
    private TaskListService taskListService;

    @Mock
    private TaskListRenderer taskListRenderer;

    @Mock
    private RespondentSolicitorTaskListRenderer respondentSolicitorTaskListRenderer;

    @Mock
    private TaskErrorService taskErrorService;

    @Mock
    private RespondentTaskErrorService respondentTaskErrorService;

    @Mock
    private AllTabServiceImpl allTabService;

    @InjectMocks
    private CaseEventHandler caseEventHandler;
    @Mock
    private  ObjectMapper objectMapper;

    public static final String authToken = "Bearer TestAuthToken";

    @Test
    public void shouldUpdateTaskListForCasesInOpenStateC100() {
        final CaseData caseData = CaseData.builder()
                .id(nextLong())
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .build();
        final CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);

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
                Task.builder().event(MIAM).state(NOT_STARTED).build()
        );

        final List<RespondentTask> respondentTask = List.of(
                RespondentTask.builder().event(CONSENT).build(),
                RespondentTask.builder().event(RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS).build()
        );

        final String c100renderedTaskList = "<h1>Case Name</h1><h2>Miam</h2>";

        final String respondentTaskListA = "<h3>Respond to the application for Respondent A";
        final String respondentTaskListB = "<h3>Respond to the application for Respondent B";

        List<RespondentEventValidationErrors> resErrors = new ArrayList<>();

        when(taskListService.getTasksForOpenCase(caseData)).thenReturn(c100Tasks);
        when(taskListRenderer.render(c100Tasks, errors, true, caseData)).thenReturn(c100renderedTaskList);
        when(respondentSolicitorTaskListRenderer.render(
                respondentTask,
                resErrors,
                "A",
                "test test", false, caseData
        )).thenReturn(respondentTaskListA);
        when(respondentSolicitorTaskListRenderer.render(
                respondentTask,
                resErrors,
                "B",
                "test test", false, caseData
        )).thenReturn(respondentTaskListB);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            stringObjectMap,
            caseData,
            null
        );
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        caseEventHandler.handleCaseDataChange(caseDataChanged);

        assertFalse(errors.contains(EventValidationErrors.builder()
                .event(FL401_TYPE_OF_APPLICATION)
                .errors(Collections.singletonList(FL401_TYPE_OF_APPLICATION_ERROR.getError()))
                .build()));

        verify(taskListService).getTasksForOpenCase(caseData);
        verify(taskListRenderer).render(c100Tasks, errors, true, caseData);
    }

    @Test
    public void shouldUpdateTaskListForCasesInOpenStateFl401() {
        final CaseData caseData = CaseData.builder()
                .id(nextLong())
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .caseTypeOfApplication(FL401_CASE_TYPE)
                .build();
        final CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);

        final List<Event> fl410Events = List.of(
                CASE_NAME,
                FL401_TYPE_OF_APPLICATION
        );

        List<EventValidationErrors> errors = new ArrayList<>();
        errors.add(EventValidationErrors.builder()
                .event(TYPE_OF_APPLICATION)
                .errors(Collections.singletonList(TYPE_OF_APPLICATION_ERROR.getError()))
                .build());
        errors.add(EventValidationErrors.builder()
                .event(RESPONDENT_BEHAVIOUR)
                .errors(Collections.singletonList(RESPONDENT_BEHAVIOUR_ERROR.getError()))
                .build());

        when(taskListService.getFL401Events(caseData)).thenReturn(fl410Events);
        when(taskErrorService.getEventErrors(caseData)).thenReturn(errors);

        final List<Task> fl401Tasks = List.of(
                Task.builder().event(CASE_NAME).state(FINISHED).build(),
                Task.builder().event(FL401_TYPE_OF_APPLICATION).state(NOT_STARTED).build()
        );

        final List<RespondentTask> respondentTask = List.of(
                RespondentTask.builder().event(CONSENT).build(),
                RespondentTask.builder().event(RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS).build()
        );

        final String fl410renderedTaskList = "<h1>Case Name</h1><h2>Type of Application</h2>";

        final List<EventValidationErrors> eventsErrors = Collections.emptyList();
        final List<RespondentEventValidationErrors> resErrors = Collections.emptyList();

        when(taskListService.getTasksForOpenCase(caseData)).thenReturn(fl401Tasks);
        when(taskListRenderer.render(fl401Tasks, eventsErrors, false, caseData)).thenReturn(fl410renderedTaskList);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            stringObjectMap,
            caseData,
            null
        );
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        caseEventHandler.handleCaseDataChange(caseDataChanged);

        assertFalse(errors.contains(EventValidationErrors.builder()
                .event(TYPE_OF_APPLICATION)
                .errors(Collections.singletonList(TYPE_OF_APPLICATION_ERROR.getError()))
                .build()));

        verify(taskListService).getTasksForOpenCase(caseData);
        verify(taskListRenderer).render(fl401Tasks, eventsErrors, false, caseData);
    }

    @Test
    public void shouldUpdateTaskListForCasesInOpenStateC100WithRespondents() {
        PartyDetails respondent = PartyDetails.builder()
                .firstName("TestFirst")
                .lastName("TestLast")
                .canYouProvideEmailAddress(YesOrNo.Yes)
                .email("respondent@tests.com")
                .isEmailAddressConfidential(YesOrNo.No)
                .isAddressConfidential(YesOrNo.No)
                .solicitorEmail("test@test.com")
                .response(Response.builder()
                        .c7ResponseSubmitted(YesOrNo.Yes)
                        .build())
                .build();
        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        final CaseData caseData = CaseData.builder()
                .id(nextLong())
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .respondents(listOfRespondents)
                .build();
        final CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);

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
                Task.builder().event(MIAM).state(NOT_STARTED).build()
        );

        final List<RespondentTask> respondentTask = List.of(
                RespondentTask.builder().event(CONSENT).build(),
                RespondentTask.builder().event(RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS).build()
        );

        final String c100renderedTaskList = "<h1>Case Name</h1><h2>Miam</h2>";

        final String respondentTaskListA = "<h3>Respond to the application for Respondent A";
        final String respondentTaskListB = "<h3>Respond to the application for Respondent B";
        List<RespondentEventValidationErrors> resErrors = new ArrayList<>();

        final List<RespondentSolicitorEvents> respondentEvents = List.of(
                CONSENT,
                KEEP_DETAILS_PRIVATE
        );

        when(taskListService.getTasksForOpenCase(caseData)).thenReturn(c100Tasks);
        when(taskListService.getRespondentsEvents(caseData)).thenReturn(respondentEvents);
        when(taskListRenderer.render(c100Tasks, errors, true, caseData)).thenReturn(c100renderedTaskList);
        when(respondentSolicitorTaskListRenderer.render(
                respondentTask,
                resErrors,
                "A",
                "test test", false, caseData
        )).thenReturn(respondentTaskListA);
        when(respondentSolicitorTaskListRenderer.render(
                respondentTask,
                resErrors,
                "B",
                "test test", false, caseData
        )).thenReturn(respondentTaskListB);


        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            stringObjectMap,
            caseData,
            null
        );
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

        caseEventHandler.handleCaseDataChange(caseDataChanged);

        assertFalse(errors.contains(EventValidationErrors.builder()
                .event(FL401_TYPE_OF_APPLICATION)
                .errors(Collections.singletonList(FL401_TYPE_OF_APPLICATION_ERROR.getError()))
                .build()));

        verify(taskListService).getTasksForOpenCase(caseData);
        verify(taskListRenderer).render(c100Tasks, errors, true, caseData);
    }

    @Test
    public void testGetRespondentTaskList() {
        List<Element<PartyDetails>> respondents = new ArrayList<>();
        respondents.add(element(PartyDetails.builder()
                .user(User.builder().solicitorRepresented(YesOrNo.Yes).build())
                .firstName("test")
                .lastName("test")
                .email("test@hmcts.net")
                .response(Response.builder().build())
                .build()));

        final CaseData caseData = CaseData.builder()
                .id(nextLong())
                .caseTypeOfApplication(C100_CASE_TYPE)
                .respondents(respondents)
                .build();
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
                Task.builder().event(MIAM).state(NOT_STARTED).build()
        );

        final List<RespondentTask> respondentTask = List.of(
                RespondentTask.builder().event(CONSENT).build(),
                RespondentTask.builder().event(RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS).build()
        );

        final String c100renderedTaskList = "<h1>Case Name</h1><h2>Miam</h2>";

        final String respondentTaskListA = "<h3>Respond to the application for Respondent A";

        List<RespondentEventValidationErrors> resErrors = new ArrayList<>();

        when(taskListService.getTasksForOpenCase(caseData)).thenReturn(c100Tasks);
        when(taskListRenderer.render(c100Tasks, errors, true, caseData)).thenReturn(c100renderedTaskList);
        when(respondentSolicitorTaskListRenderer.render(
                respondentTask,
                resErrors,
                "A",
                "test test", false, caseData
        )).thenReturn(respondentTaskListA);

        caseEventHandler.getRespondentTaskList(caseData, "A");

        verify(respondentSolicitorTaskListRenderer).render(Mockito.anyList(), Mockito.anyList(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyBoolean(), Mockito.any()
        );

    }

    @Test
    public void testGetRespondentTaskListWhenC7ResponseSubmitted() {
        List<Element<PartyDetails>> respondents = new ArrayList<>();
        respondents.add(element(PartyDetails.builder()
                .user(User.builder().solicitorRepresented(YesOrNo.Yes).build())
                .firstName("test")
                .lastName("test")
                .response(Response.builder()
                        .c7ResponseSubmitted(YesOrNo.Yes).build())
                .email("test@hmcts.net")
                .build()));

        final CaseData caseData = CaseData.builder()
                .id(nextLong())
                .state(State.AWAITING_RESUBMISSION_TO_HMCTS)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .respondents(respondents)
                .build();
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
                Task.builder().event(MIAM).state(NOT_STARTED).build()
        );

        final List<RespondentTask> respondentTask = List.of(
                RespondentTask.builder().event(CONSENT).build(),
                RespondentTask.builder().event(RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS).build()
        );

        final String c100renderedTaskList = "<h1>Case Name</h1><h2>Miam</h2>";

        final String respondentTaskListA = "<h3>Respond to the application for Respondent A";

        List<RespondentEventValidationErrors> resErrors = new ArrayList<>();

        when(taskListService.getTasksForOpenCase(caseData)).thenReturn(c100Tasks);
        when(taskListRenderer.render(c100Tasks, errors, true, caseData)).thenReturn(c100renderedTaskList);
        when(respondentSolicitorTaskListRenderer.render(
                respondentTask,
                resErrors,
                "A",
                "test test", false, caseData
        )).thenReturn(respondentTaskListA);

        caseEventHandler.getRespondentTaskList(caseData, "A");

        verify(respondentSolicitorTaskListRenderer).render(Mockito.any(), Mockito.any(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyBoolean(), Mockito.any()
        );

    }


    @Test
    public void shouldUpdateTaskListForCasesInReturnedStateC100() {
        final CaseData caseData = CaseData.builder()
                .id(nextLong())
                .state(State.AWAITING_RESUBMISSION_TO_HMCTS)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .build();
        final CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);

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
                Task.builder().event(MIAM).state(NOT_STARTED).build()
        );

        final List<RespondentTask> respondentTask = List.of(
                RespondentTask.builder().event(CONSENT).build(),
                RespondentTask.builder().event(RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS).build()
        );

        final String c100renderedTaskList = "<h1>Case Name</h1><h2>Miam</h2>";

        final String respondentTaskListA = "<h3>Respond to the application for Respondent A";
        final String respondentTaskListB = "<h3>Respond to the application for Respondent B";

        List<RespondentEventValidationErrors> resErrors = new ArrayList<>();

        when(taskListService.getTasksForOpenCase(caseData)).thenReturn(c100Tasks);
        when(taskListRenderer.render(c100Tasks, errors, true, caseData)).thenReturn(c100renderedTaskList);
        when(respondentSolicitorTaskListRenderer.render(
                respondentTask,
                resErrors,
                "A",
                "test test", false, caseData
        )).thenReturn(respondentTaskListA);
        when(respondentSolicitorTaskListRenderer.render(
                respondentTask,
                resErrors,
                "B",
                "test test", false, caseData
        )).thenReturn(respondentTaskListB);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            stringObjectMap,
            caseData,
            null
        );
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        caseEventHandler.handleCaseDataChange(caseDataChanged);

        assertFalse(errors.contains(EventValidationErrors.builder()
                .event(FL401_TYPE_OF_APPLICATION)
                .errors(Collections.singletonList(FL401_TYPE_OF_APPLICATION_ERROR.getError()))
                .build()));

        verify(taskListService).getTasksForOpenCase(caseData);
        verify(taskListRenderer).render(c100Tasks, errors, true, caseData);
    }
}
