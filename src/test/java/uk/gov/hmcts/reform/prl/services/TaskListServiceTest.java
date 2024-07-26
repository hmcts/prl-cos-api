package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.models.tasklist.RespondentTask;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators.RespondentEventsChecker;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM_REVISED;
import static uk.gov.hmcts.reform.prl.enums.Event.AMEND_MIAM_POLICY_UPGRADE;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_APPLICANTS;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS_REVISED;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_APPLICANT_FAMILY_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_HOME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_RESUBMIT;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_SOT_AND_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_UPLOAD_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.prl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.LITIGATION_CAPACITY;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM_POLICY_UPGRADE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE_REVISED;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.RELATIONSHIP_TO_RESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_BEHAVIOUR;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.Event.SUBMIT_AND_PAY;
import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.VIEW_PDF_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.WELSH_LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.WITHOUT_NOTICE_ORDER;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ABILITY_TO_PARTICIPATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ATTENDING_THE_COURT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONSENT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.RESPOND_ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.VIEW_DRAFT_RESPONSE;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.CANNOT_START_YET;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.FINISHED;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.IN_PROGRESS;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.MANDATORY_COMPLETED;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.NOT_STARTED;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class TaskListServiceTest {

    private TypeOfApplicationOrders orders;
    private LinkToCA linkToCA;

    @InjectMocks
    TaskListService taskListService;

    @Mock
    EventsChecker eventsChecker;

    @Mock
    RespondentEventsChecker respondentEventsChecker;

    @Mock
    EventService eventPublisher;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    RoleAssignmentApi roleAssignmentApi;

    @Mock
    RoleAssignmentServiceResponse roleAssignmentServiceResponse;

    @Mock
    LaunchDarklyClient launchDarklyClient;

    public static final String authToken = "Bearer TestAuthToken";

    @Mock
    UserService userService;

    @Mock
    DocumentGenService dgsService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    AllTabServiceImpl tabService;

    private RoleAssignmentServiceResponse setAndGetRoleAssignmentServiceResponse(String roleName) {
        List<RoleAssignmentResponse> listOfRoleAssignmentResponses = new ArrayList<>();
        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName(roleName);
        listOfRoleAssignmentResponses.add(roleAssignmentResponse);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = new RoleAssignmentServiceResponse();
        roleAssignmentServiceResponse.setRoleAssignmentResponse(listOfRoleAssignmentResponses);
        return roleAssignmentServiceResponse;
    }

    @Test
    public void getTasksShouldReturnListOfTasks() {

        CaseData caseData = CaseData.builder().caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();

        List<Task> expectedTasks = List.of(
                Task.builder().event(CASE_NAME).state(NOT_STARTED).state(NOT_STARTED).build(),
                Task.builder().event(TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
                Task.builder().event(HEARING_URGENCY).state(NOT_STARTED).build(),
                Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(CHILD_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(MIAM).state(NOT_STARTED).build(),
                Task.builder().event(ALLEGATIONS_OF_HARM).state(NOT_STARTED).build(),
                Task.builder().event(OTHER_PEOPLE_IN_THE_CASE).state(NOT_STARTED).build(),
                Task.builder().event(OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
                Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
                Task.builder().event(INTERNATIONAL_ELEMENT).state(NOT_STARTED).build(),
                Task.builder().event(LITIGATION_CAPACITY).state(NOT_STARTED).build(),
                Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
                Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
                Task.builder().event(SUBMIT_AND_PAY).state(NOT_STARTED).build(),
                Task.builder().event(SUBMIT).state(NOT_STARTED).build());
        Mockito.when(eventsChecker.getDefaultState(Mockito.any(Event.class),Mockito.any(CaseData.class))).thenReturn(NOT_STARTED);

        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);
    }


    @Test
    public void getTasksShouldReturnListOfRespondentSolicitorTasks() {
        Document document = Document.builder()
                .documentUrl("https:google.com")
                .build();
        CaseData caseData = CaseData.builder()
                .c1ADocument(document)
                .build();
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
                .representativeLastName("Xyz")
                .gender(Gender.male)
                .email("abc@xyz.com")
                .phoneNumber("1234567890")
                .canYouProvideEmailAddress(Yes)
                .isEmailAddressConfidential(Yes)
                .isPhoneNumberConfidential(Yes)
                .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
                .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .build();
        List<RespondentTask> expectedTasks = List.of(
                RespondentTask.builder().event(CONSENT).state(TaskState.NOT_STARTED).build(),
                RespondentTask.builder().event(KEEP_DETAILS_PRIVATE).state(TaskState.NOT_STARTED).build(),
                RespondentTask.builder().event(CONFIRM_EDIT_CONTACT_DETAILS).state(TaskState.NOT_STARTED).build(),
                RespondentTask.builder().event(ATTENDING_THE_COURT).state(TaskState.NOT_STARTED).build(),
                RespondentTask.builder().event(RespondentSolicitorEvents.MIAM).state(TaskState.NOT_STARTED).build(),
            RespondentTask.builder().event(RespondentSolicitorEvents.OTHER_PROCEEDINGS).state(TaskState.NOT_STARTED).build(),
                RespondentTask.builder().event(ALLEGATION_OF_HARM).state(TaskState.NOT_STARTED).build(),
                RespondentTask.builder().event(RESPOND_ALLEGATION_OF_HARM).state(TaskState.NOT_STARTED).build(),
                RespondentTask.builder().event(RespondentSolicitorEvents.INTERNATIONAL_ELEMENT).state(TaskState.NOT_STARTED).build(),
                RespondentTask.builder().event(ABILITY_TO_PARTICIPATE).state(TaskState.NOT_STARTED).build(),
                RespondentTask.builder().event(VIEW_DRAFT_RESPONSE).state(TaskState.NOT_STARTED).build(),
                RespondentTask.builder().event(RespondentSolicitorEvents.SUBMIT).state(TaskState.NOT_STARTED).build()
        );

        List<RespondentTask> actualTasks = taskListService.getRespondentSolicitorTasks(applicant, caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);
    }

    @Test
    public void getTasksShouldReturnListOfRespondentSolicitorFinishedTasks() {
        Document document = Document.builder()
            .documentUrl("https:google.com")
            .build();
        CaseData caseData = CaseData.builder()
            .c1ADocument(document)
            .build();
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        List<RespondentTask> expectedTasks = List.of(
            RespondentTask.builder().event(CONSENT).state(TaskState.FINISHED).build(),
            RespondentTask.builder().event(KEEP_DETAILS_PRIVATE).state(TaskState.FINISHED).build(),
            RespondentTask.builder().event(CONFIRM_EDIT_CONTACT_DETAILS).state(TaskState.FINISHED).build(),
            RespondentTask.builder().event(ATTENDING_THE_COURT).state(TaskState.FINISHED).build(),
            RespondentTask.builder().event(RespondentSolicitorEvents.MIAM).state(TaskState.FINISHED).build(),
            RespondentTask.builder().event(RespondentSolicitorEvents.OTHER_PROCEEDINGS).state(TaskState.FINISHED).build(),
            RespondentTask.builder().event(ALLEGATION_OF_HARM).state(TaskState.FINISHED).build(),
            RespondentTask.builder().event(RESPOND_ALLEGATION_OF_HARM).state(TaskState.FINISHED).build(),
            RespondentTask.builder().event(RespondentSolicitorEvents.INTERNATIONAL_ELEMENT).state(TaskState.FINISHED).build(),
            RespondentTask.builder().event(ABILITY_TO_PARTICIPATE).state(TaskState.FINISHED).build(),
            RespondentTask.builder().event(VIEW_DRAFT_RESPONSE).state(TaskState.FINISHED).build(),
            RespondentTask.builder().event(RespondentSolicitorEvents.SUBMIT).state(TaskState.FINISHED).build()
        );
        when(respondentEventsChecker.isFinished(any(), any(), anyBoolean())).thenReturn(true);
        List<RespondentTask> actualTasks = taskListService.getRespondentSolicitorTasks(applicant, caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);
    }

    @Test
    public void getTasksShouldReturnListOfRespondentSolicitorInProgressTasks() {
        Document document = Document.builder()
            .documentUrl("https:google.com")
            .build();
        CaseData caseData = CaseData.builder()
            .c1ADocument(document)
            .build();
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        List<RespondentTask> expectedTasks = List.of(
            RespondentTask.builder().event(CONSENT).state(TaskState.IN_PROGRESS).build(),
            RespondentTask.builder().event(KEEP_DETAILS_PRIVATE).state(TaskState.IN_PROGRESS).build(),
            RespondentTask.builder().event(CONFIRM_EDIT_CONTACT_DETAILS).state(TaskState.IN_PROGRESS).build(),
            RespondentTask.builder().event(ATTENDING_THE_COURT).state(TaskState.IN_PROGRESS).build(),
            RespondentTask.builder().event(RespondentSolicitorEvents.MIAM).state(TaskState.IN_PROGRESS).build(),
            RespondentTask.builder().event(RespondentSolicitorEvents.OTHER_PROCEEDINGS).state(TaskState.IN_PROGRESS).build(),
            RespondentTask.builder().event(ALLEGATION_OF_HARM).state(TaskState.IN_PROGRESS).build(),
            RespondentTask.builder().event(RESPOND_ALLEGATION_OF_HARM).state(TaskState.IN_PROGRESS).build(),
            RespondentTask.builder().event(RespondentSolicitorEvents.INTERNATIONAL_ELEMENT).state(TaskState.IN_PROGRESS).build(),
            RespondentTask.builder().event(ABILITY_TO_PARTICIPATE).state(TaskState.IN_PROGRESS).build(),
            RespondentTask.builder().event(VIEW_DRAFT_RESPONSE).state(TaskState.IN_PROGRESS).build(),
            RespondentTask.builder().event(RespondentSolicitorEvents.SUBMIT).state(TaskState.IN_PROGRESS).build()
        );

        when(respondentEventsChecker.isFinished(any(), any(), anyBoolean())).thenReturn(false);
        when(respondentEventsChecker.isStarted(any(), any(), anyBoolean())).thenReturn(true);
        List<RespondentTask> actualTasks = taskListService.getRespondentSolicitorTasks(applicant, caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);
    }

    @Test
    public void getTasksShouldReturnListOfTasks_WithNewAllegationOfHarm() {

        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .taskListVersion(TASK_LIST_VERSION_V2).build();

        List<Task> expectedTasks = List.of(
                Task.builder().event(CASE_NAME).build(),
                Task.builder().event(TYPE_OF_APPLICATION).build(),
                Task.builder().event(HEARING_URGENCY).build(),
                Task.builder().event(CHILD_DETAILS_REVISED).build(),
                Task.builder().event(APPLICANT_DETAILS).build(),
                Task.builder().event(RESPONDENT_DETAILS).build(),
                Task.builder().event(OTHER_PEOPLE_IN_THE_CASE_REVISED).build(),
                Task.builder().event(OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION).build(),
                Task.builder().event(CHILDREN_AND_APPLICANTS).state(CANNOT_START_YET).build(),
                Task.builder().event(CHILDREN_AND_RESPONDENTS).state(CANNOT_START_YET).build(),
                Task.builder().event(CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION).state(CANNOT_START_YET).build(),
                Task.builder().event(ALLEGATIONS_OF_HARM_REVISED).build(),
                Task.builder().event(MIAM).build(),
                Task.builder().event(OTHER_PROCEEDINGS).build(),
                Task.builder().event(ATTENDING_THE_HEARING).build(),
                Task.builder().event(INTERNATIONAL_ELEMENT).build(),
                Task.builder().event(LITIGATION_CAPACITY).build(),
                Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).build(),
                Task.builder().event(VIEW_PDF_DOCUMENT).build(),
                Task.builder().event(SUBMIT_AND_PAY).build(),
                Task.builder().event(SUBMIT).build());
        when(eventsChecker.getDefaultState(CHILDREN_AND_APPLICANTS,caseData)).thenReturn(CANNOT_START_YET);
        when(eventsChecker.getDefaultState(CHILDREN_AND_RESPONDENTS,caseData)).thenReturn(CANNOT_START_YET);
        when(eventsChecker.getDefaultState(CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION,caseData)).thenReturn(CANNOT_START_YET);
        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);
    }


    @Test
    public void getTasksShouldReturnFl401ListOfTasks() {

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.nonMolestationOrder);

        orders = TypeOfApplicationOrders.builder()
                .orderType(orderList)
                .build();

        linkToCA = LinkToCA.builder()
                .linkToCaApplication(YesOrNo.Yes)
                .caApplicationNumber("123")
                .build();
        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
                .typeOfApplicationOrders(orders)
                .typeOfApplicationLinkToCA(linkToCA).build();

        List<Task> expectedTasks = List.of(
                Task.builder().event(FL401_CASE_NAME).state(NOT_STARTED).build(),
                Task.builder().event(FL401_TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
                Task.builder().event(WITHOUT_NOTICE_ORDER).state(NOT_STARTED).build(),
                Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(FL401_APPLICANT_FAMILY_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(RELATIONSHIP_TO_RESPONDENT).state(NOT_STARTED).build(),
                Task.builder().event(FL401_OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
                Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
                Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
                Task.builder().event(FL401_UPLOAD_DOCUMENTS).state(NOT_STARTED).build(),
                Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
                Task.builder().event(FL401_SOT_AND_SUBMIT).state(NOT_STARTED).build(),
                Task.builder().event(FL401_RESUBMIT).state(NOT_STARTED).build(),
                Task.builder().event(RESPONDENT_BEHAVIOUR).state(NOT_STARTED).build()
        );
        Mockito.when(eventsChecker.getDefaultState(Mockito.any(Event.class),Mockito.any(CaseData.class))).thenReturn(NOT_STARTED);
        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);

    }

    @Test
    public void getTasksShouldReturnOccupationOrderFl401ListOfTasks() {

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.occupationOrder);

        orders = TypeOfApplicationOrders.builder()
                .orderType(orderList)
                .build();

        linkToCA = LinkToCA.builder()
                .linkToCaApplication(YesOrNo.Yes)
                .caApplicationNumber("123")
                .build();
        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
                .typeOfApplicationOrders(orders)
                .typeOfApplicationLinkToCA(linkToCA)
                .build();

        List<Task> expectedTasks = List.of(
                Task.builder().event(FL401_CASE_NAME).state(NOT_STARTED).build(),
                Task.builder().event(FL401_TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
                Task.builder().event(WITHOUT_NOTICE_ORDER).state(NOT_STARTED).build(),
                Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(FL401_APPLICANT_FAMILY_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(RELATIONSHIP_TO_RESPONDENT).state(NOT_STARTED).build(),
                Task.builder().event(FL401_OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
                Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
                Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
                Task.builder().event(FL401_UPLOAD_DOCUMENTS).state(NOT_STARTED).build(),
                Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
                Task.builder().event(FL401_SOT_AND_SUBMIT).state(NOT_STARTED).build(),
                Task.builder().event(FL401_RESUBMIT).state(NOT_STARTED).build(),
                Task.builder().event(FL401_HOME).state(NOT_STARTED).build());
        Mockito.when(eventsChecker.getDefaultState(Mockito.any(Event.class),Mockito.any(CaseData.class))).thenReturn(NOT_STARTED);
        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);

    }

    @Test
    public void getTasksShouldReturnOccupationAndNonMolestationOrderFl401ListOfTasks() {

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.occupationOrder);
        orderList.add(FL401OrderTypeEnum.nonMolestationOrder);

        orders = TypeOfApplicationOrders.builder()
                .orderType(orderList)
                .build();

        linkToCA = LinkToCA.builder()
                .linkToCaApplication(YesOrNo.Yes)
                .caApplicationNumber("123")
                .build();
        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
                .typeOfApplicationOrders(orders)
                .typeOfApplicationLinkToCA(linkToCA)
                .build();

        List<Task> expectedTasks = List.of(
                Task.builder().event(FL401_CASE_NAME).state(NOT_STARTED).build(),
                Task.builder().event(FL401_TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
                Task.builder().event(WITHOUT_NOTICE_ORDER).state(NOT_STARTED).build(),
                Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(FL401_APPLICANT_FAMILY_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(RELATIONSHIP_TO_RESPONDENT).state(NOT_STARTED).build(),
                Task.builder().event(FL401_OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
                Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
                Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
                Task.builder().event(FL401_UPLOAD_DOCUMENTS).state(NOT_STARTED).build(),
                Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
                Task.builder().event(FL401_SOT_AND_SUBMIT).state(NOT_STARTED).build(),
                Task.builder().event(FL401_RESUBMIT).state(NOT_STARTED).build(),
                Task.builder().event(RESPONDENT_BEHAVIOUR).state(NOT_STARTED).build(),
                Task.builder().event(FL401_HOME).state(NOT_STARTED).build());
        Mockito.when(eventsChecker.getDefaultState(Mockito.any(Event.class),Mockito.any(CaseData.class))).thenReturn(NOT_STARTED);
        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);
    }

    @Test
    public void getTasksShouldReturnListOfTasksForCaseNameEventFinished() {

        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .applicantName("test")
                .build();

        List<Task> expectedTasks = List.of(
                Task.builder().event(CASE_NAME).state(FINISHED).state(FINISHED).build(),
                Task.builder().event(TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
                Task.builder().event(HEARING_URGENCY).state(NOT_STARTED).build(),
                Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(CHILD_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(MIAM).state(NOT_STARTED).build(),
                Task.builder().event(ALLEGATIONS_OF_HARM).state(NOT_STARTED).build(),
                Task.builder().event(OTHER_PEOPLE_IN_THE_CASE).state(NOT_STARTED).build(),
                Task.builder().event(OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
                Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
                Task.builder().event(INTERNATIONAL_ELEMENT).state(NOT_STARTED).build(),
                Task.builder().event(LITIGATION_CAPACITY).state(NOT_STARTED).build(),
                Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
                Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
                Task.builder().event(SUBMIT_AND_PAY).state(NOT_STARTED).build(),
                Task.builder().event(SUBMIT).state(NOT_STARTED).build()
        );
        Event event = CASE_NAME;
        when(eventsChecker.isFinished(event, caseData)).thenReturn(true);
        when(eventsChecker.getDefaultState(Mockito.any(),Mockito.any(CaseData.class))).thenReturn(NOT_STARTED);
        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);
    }

    @Test
    public void getTasksShouldReturnListOfTasksForTypeOfAppEventInProgress() {

        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .consentOrder(YesOrNo.Yes)
                .build();

        List<Task> expectedTasks = List.of(
                Task.builder().event(CASE_NAME).state(NOT_STARTED).state(NOT_STARTED).build(),
                Task.builder().event(TYPE_OF_APPLICATION).state(IN_PROGRESS).build(),
                Task.builder().event(HEARING_URGENCY).state(NOT_STARTED).build(),
                Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(CHILD_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(MIAM).state(NOT_STARTED).build(),
                Task.builder().event(ALLEGATIONS_OF_HARM).state(NOT_STARTED).build(),
                Task.builder().event(OTHER_PEOPLE_IN_THE_CASE).state(NOT_STARTED).build(),
                Task.builder().event(OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
                Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
                Task.builder().event(INTERNATIONAL_ELEMENT).state(NOT_STARTED).build(),
                Task.builder().event(LITIGATION_CAPACITY).state(NOT_STARTED).build(),
                Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
                Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
                Task.builder().event(SUBMIT_AND_PAY).state(NOT_STARTED).build(),
                Task.builder().event(SUBMIT).state(NOT_STARTED).build()
        );

        Event event = TYPE_OF_APPLICATION;
        when(eventsChecker.isStarted(event, caseData)).thenReturn(true);
        when(eventsChecker.getDefaultState(Mockito.any(),Mockito.any(CaseData.class))).thenReturn(NOT_STARTED);
        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);
    }

    @Test
    public void getTasksShouldReturnListOfTasksForTypeOfAppEventMandatoryCompleted() {

        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .consentOrder(YesOrNo.Yes)
                .build();

        List<Task> expectedTasks = List.of(
                Task.builder().event(CASE_NAME).state(NOT_STARTED).state(NOT_STARTED).build(),
                Task.builder().event(TYPE_OF_APPLICATION).state(MANDATORY_COMPLETED).build(),
                Task.builder().event(HEARING_URGENCY).state(NOT_STARTED).build(),
                Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(CHILD_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
                Task.builder().event(MIAM).state(NOT_STARTED).build(),
                Task.builder().event(ALLEGATIONS_OF_HARM).state(NOT_STARTED).build(),
                Task.builder().event(OTHER_PEOPLE_IN_THE_CASE).state(NOT_STARTED).build(),
                Task.builder().event(OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
                Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
                Task.builder().event(INTERNATIONAL_ELEMENT).state(NOT_STARTED).build(),
                Task.builder().event(LITIGATION_CAPACITY).state(NOT_STARTED).build(),
                Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
                Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
                Task.builder().event(SUBMIT_AND_PAY).state(NOT_STARTED).build(),
                Task.builder().event(SUBMIT).state(NOT_STARTED).build()
        );

        Event event = TYPE_OF_APPLICATION;
        when(eventsChecker.hasMandatoryCompleted(event, caseData)).thenReturn(true);
        when(eventsChecker.getDefaultState(Mockito.any(),Mockito.any(CaseData.class))).thenReturn(NOT_STARTED);
        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);
    }

    @Test
    public void testGetRespondentsEvents() {
        CaseData caseData = CaseData.builder()
                .c1ADocument(null)
                .build();
        List<RespondentSolicitorEvents> actualRespEvents = taskListService.getRespondentsEvents(caseData);

        List<RespondentSolicitorEvents> expectedRespEvents = List.of(
                CONSENT,
                KEEP_DETAILS_PRIVATE,
                CONFIRM_EDIT_CONTACT_DETAILS,
                ATTENDING_THE_COURT,
                RespondentSolicitorEvents.MIAM,
                RespondentSolicitorEvents.OTHER_PROCEEDINGS,
                RespondentSolicitorEvents.ALLEGATION_OF_HARM,
                RespondentSolicitorEvents.INTERNATIONAL_ELEMENT,
                ABILITY_TO_PARTICIPATE,
                VIEW_DRAFT_RESPONSE,
                RespondentSolicitorEvents.SUBMIT
        );
        assertThat(expectedRespEvents).isEqualTo(actualRespEvents);
    }


    @Test
    public void testGetRespondentsEventsWhenAllegationofHarmisPresent() {
        Document document = Document.builder()
                .documentUrl("https:google.com")
                .build();
        CaseData caseData = CaseData.builder()
                .c1ADocument(document)
                .build();
        List<RespondentSolicitorEvents> actualRespEvents = taskListService.getRespondentsEvents(caseData);

        List<RespondentSolicitorEvents> expectedRespEvents = List.of(
                CONSENT,
                KEEP_DETAILS_PRIVATE,
                CONFIRM_EDIT_CONTACT_DETAILS,
                ATTENDING_THE_COURT,
                RespondentSolicitorEvents.MIAM,
                RespondentSolicitorEvents.OTHER_PROCEEDINGS,
                RespondentSolicitorEvents.ALLEGATION_OF_HARM,
                RESPOND_ALLEGATION_OF_HARM,
                RespondentSolicitorEvents.INTERNATIONAL_ELEMENT,
                ABILITY_TO_PARTICIPATE,
                VIEW_DRAFT_RESPONSE,
                RespondentSolicitorEvents.SUBMIT
        );
        assertThat(expectedRespEvents).isEqualTo(actualRespEvents);
    }

    @Test
    public void getTasksShouldReturnListOfTasksVersion2Relations() {

        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .consentOrder(YesOrNo.Yes)
                .taskListVersion(TASK_LIST_VERSION_V2)
                .build();

        List<Task> expectedTasks = List.of(
                Task.builder().event(CASE_NAME).build(),
                Task.builder().event(TYPE_OF_APPLICATION).build(),
                Task.builder().event(HEARING_URGENCY).build(),
                Task.builder().event(CHILD_DETAILS_REVISED).build(),
                Task.builder().event(APPLICANT_DETAILS).build(),
                Task.builder().event(RESPONDENT_DETAILS).build(),
                Task.builder().event(OTHER_PEOPLE_IN_THE_CASE_REVISED).build(),
                Task.builder().event(OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION).build(),
                Task.builder().event(CHILDREN_AND_APPLICANTS).state(CANNOT_START_YET).build(),
                Task.builder().event(CHILDREN_AND_RESPONDENTS).state(CANNOT_START_YET).build(),
                Task.builder().event(CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION).state(CANNOT_START_YET).build(),
                Task.builder().event(ALLEGATIONS_OF_HARM_REVISED).build(),
                Task.builder().event(MIAM).build(),
                Task.builder().event(OTHER_PROCEEDINGS).build(),
                Task.builder().event(ATTENDING_THE_HEARING).build(),
                Task.builder().event(INTERNATIONAL_ELEMENT).build(),
                Task.builder().event(LITIGATION_CAPACITY).build(),
                Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).build(),
                Task.builder().event(VIEW_PDF_DOCUMENT).build(),
                Task.builder().event(SUBMIT_AND_PAY).build(),
                Task.builder().event(SUBMIT).build());
        when(eventsChecker.getDefaultState(CHILDREN_AND_APPLICANTS,caseData)).thenReturn(CANNOT_START_YET);
        when(eventsChecker.getDefaultState(CHILDREN_AND_RESPONDENTS,caseData)).thenReturn(CANNOT_START_YET);
        when(eventsChecker.getDefaultState(CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION,caseData)).thenReturn(CANNOT_START_YET);
        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);
    }

    @Test
    public void updateTaskListAsCourtAdminWhenCaseIsInSubmittedState() throws Exception {
        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .typeOfApplicationOrders(orders)
                .typeOfApplicationLinkToCA(linkToCA)
                .state(State.SUBMITTED_PAID)
                .build();


        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        Map<String, Object> documentMap = new HashMap<>();
        stringObjectMap.putAll(documentMap);

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap, caseData, null);
        when(tabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(userService.getUserDetails(authToken))
                .thenReturn(UserDetails.builder().roles(List.of("caseworker-privatelaw-courtadmin")).build());
        when(dgsService.generateDocuments(authToken, caseData)).thenReturn(documentMap);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(
            "senior-tribunal-caseworker");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);
        when(roleAssignmentApi.getRoleAssignments(authToken, authTokenGenerator.generate(), null, null)).thenReturn(
            roleAssignmentServiceResponse);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .state("SUBMITTED_PAID")
                        .build())
                .build();
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = taskListService
                .updateTaskList(callbackRequest, authToken);
        Assert.assertNotNull(aboutToStartOrSubmitCallbackResponse);
        Assert.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
        Assert.assertEquals("SUBMITTED_PAID", aboutToStartOrSubmitCallbackResponse.getData().get("state"));
    }

    @Test
    public void updateTaskListAsCourtAdminWhenCaseIsInIssuedState() throws Exception {
        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .typeOfApplicationOrders(orders)
                .typeOfApplicationLinkToCA(linkToCA)
                .state(State.CASE_ISSUED)
                .build();


        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        Map<String, Object> documentMap = new HashMap<>();
        stringObjectMap.putAll(documentMap);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(
            "ctsc");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);
        when(roleAssignmentApi.getRoleAssignments(authToken, authTokenGenerator.generate(), null, null)).thenReturn(
            roleAssignmentServiceResponse);
        when(userService.getUserDetails(authToken))
                .thenReturn(UserDetails.builder().roles(List.of("caseworker-privatelaw-courtadmin")).build());
        when(dgsService.generateDocuments(authToken, caseData)).thenReturn(documentMap);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .state("CASE_ISSUED")
                        .build())
                .build();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap, caseData, null);
        when(tabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = taskListService
                .updateTaskList(callbackRequest, authToken);
        Assert.assertNotNull(aboutToStartOrSubmitCallbackResponse);
        Assert.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
        Assert.assertEquals("CASE_ISSUED", aboutToStartOrSubmitCallbackResponse.getData().get("state"));
    }

    @Test
    public void testNoEventPublishedAsSolicitorWhenCaseIsInSubmittedState() throws Exception {
        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .typeOfApplicationOrders(orders)
                .typeOfApplicationLinkToCA(linkToCA)
                .state(State.SUBMITTED_PAID)
                .build();

        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(
            "caseworker-privatelaw-solicitor");
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        Map<String, Object> documentMap = new HashMap<>();
        stringObjectMap.putAll(documentMap);
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);
        when(roleAssignmentApi.getRoleAssignments(authToken, authTokenGenerator.generate(), null, null)).thenReturn(
            roleAssignmentServiceResponse);
        when(userService.getUserDetails(authToken))
                .thenReturn(UserDetails.builder().roles(List.of("caseworker-privatelaw-solicitor")).build());
        //when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .state("SUBMITTED_PAID")
                        .build())
                .build();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap, caseData, null);
        when(tabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = taskListService
                .updateTaskList(callbackRequest, authToken);
        Assert.assertNotNull(aboutToStartOrSubmitCallbackResponse);
        Assert.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
        Assert.assertEquals("SUBMITTED_PAID", aboutToStartOrSubmitCallbackResponse.getData().get("state"));
    }

    @Test
    public void testErrorWhenFailedIDocumentGeneration() throws Exception {
        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .typeOfApplicationOrders(orders)
                .typeOfApplicationLinkToCA(linkToCA)
                .state(State.SUBMITTED_PAID)
                .build();


        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        Map<String, Object> documentMap = new HashMap<>();
        stringObjectMap.putAll(documentMap);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(
            "judge");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);
        when(roleAssignmentApi.getRoleAssignments(authToken, authTokenGenerator.generate(), null, null)).thenReturn(
            roleAssignmentServiceResponse);
        when(userService.getUserDetails(authToken))
                .thenReturn(UserDetails.builder().roles(List.of("caseworker-privatelaw-courtadmin")).build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(dgsService.generateDocuments(authToken, caseData)).thenReturn(documentMap);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .state("SUBMITTED_PAID")
                        .build())
                .build();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap, caseData, null);
        when(tabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = taskListService
                .updateTaskList(callbackRequest, authToken);
        Assert.assertNotNull(aboutToStartOrSubmitCallbackResponse);
        Assert.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
        Assert.assertEquals("SUBMITTED_PAID", aboutToStartOrSubmitCallbackResponse.getData().get("state"));
    }

    @Test
    public void updateTaskListForAmendMiamWhenCaseIsInIssuedState() throws Exception {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .state(State.CASE_ISSUED)
            .build();


        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        Map<String, Object> documentMap = new HashMap<>();
        stringObjectMap.putAll(documentMap);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(
            "ctsc");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);
        when(roleAssignmentApi.getRoleAssignments(authToken, authTokenGenerator.generate(), null, null)).thenReturn(
            roleAssignmentServiceResponse);
        when(userService.getUserDetails(authToken))
            .thenReturn(UserDetails.builder().roles(List.of("caseworker-privatelaw-courtadmin")).build());
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .state("CASE_ISSUED")
                             .build())
            .eventId(AMEND_MIAM_POLICY_UPGRADE.getId())
            .build();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap, caseData, null);
        when(tabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = taskListService
            .updateTaskList(callbackRequest, authToken);
        Assert.assertNotNull(aboutToStartOrSubmitCallbackResponse);
        Assert.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
        Assert.assertEquals("CASE_ISSUED", aboutToStartOrSubmitCallbackResponse.getData().get("state"));
    }

    @Test
    public void getTasksShouldReturnListOfTasksVersion3Relations() {

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .consentOrder(YesOrNo.Yes)
            .taskListVersion(TASK_LIST_VERSION_V3)
            .build();

        List<Task> expectedTasks = List.of(
            Task.builder().event(CASE_NAME).build(),
            Task.builder().event(TYPE_OF_APPLICATION).build(),
            Task.builder().event(HEARING_URGENCY).build(),
            Task.builder().event(APPLICANT_DETAILS).build(),
            Task.builder().event(RESPONDENT_DETAILS).build(),
            Task.builder().event(OTHER_PEOPLE_IN_THE_CASE_REVISED).build(),
            Task.builder().event(CHILD_DETAILS_REVISED).build(),
            Task.builder().event(OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION).build(),
            Task.builder().event(CHILDREN_AND_APPLICANTS).state(CANNOT_START_YET).build(),
            Task.builder().event(CHILDREN_AND_RESPONDENTS).state(CANNOT_START_YET).build(),
            Task.builder().event(CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION).state(CANNOT_START_YET).build(),
            Task.builder().event(ALLEGATIONS_OF_HARM_REVISED).build(),
            Task.builder().event(MIAM_POLICY_UPGRADE).build(),
            Task.builder().event(OTHER_PROCEEDINGS).build(),
            Task.builder().event(ATTENDING_THE_HEARING).build(),
            Task.builder().event(INTERNATIONAL_ELEMENT).build(),
            Task.builder().event(LITIGATION_CAPACITY).build(),
            Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).build(),
            Task.builder().event(VIEW_PDF_DOCUMENT).build(),
            Task.builder().event(SUBMIT_AND_PAY).build(),
            Task.builder().event(SUBMIT).build());
        when(eventsChecker.getDefaultState(CHILDREN_AND_APPLICANTS,caseData)).thenReturn(CANNOT_START_YET);
        when(eventsChecker.getDefaultState(CHILDREN_AND_RESPONDENTS,caseData)).thenReturn(CANNOT_START_YET);
        when(eventsChecker.getDefaultState(CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION,caseData)).thenReturn(CANNOT_START_YET);
        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);
    }
}
