package uk.gov.hmcts.reform.prl.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.RespondentTask;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators.RespondentEventsChecker;
import uk.gov.hmcts.reform.prl.services.validators.EventsChecker;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
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
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE;
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
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CURRENT_OR_PREVIOUS_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.VIEW_DRAFT_RESPONSE;
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
            Task.builder().event(SUBMIT).state(NOT_STARTED).build()
        );

        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);
    }


    @Test
    public void getTasksShouldReturnListOfRespondentSolicitorTasks() {
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
            RespondentTask.builder().event(CURRENT_OR_PREVIOUS_PROCEEDINGS).state(TaskState.NOT_STARTED).build(),
            RespondentTask.builder().event(ALLEGATION_OF_HARM).state(TaskState.NOT_STARTED).build(),
            RespondentTask.builder().event(RespondentSolicitorEvents.INTERNATIONAL_ELEMENT).state(TaskState.NOT_STARTED).build(),
            RespondentTask.builder().event(ABILITY_TO_PARTICIPATE).state(TaskState.NOT_STARTED).build(),
            RespondentTask.builder().event(VIEW_DRAFT_RESPONSE).state(TaskState.NOT_STARTED).build(),
            RespondentTask.builder().event(RespondentSolicitorEvents.SUBMIT).state(TaskState.NOT_STARTED).build()
        );

        List<RespondentTask> actualTasks = taskListService.getRespondentSolicitorTasks(applicant);

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
            Task.builder().event(FL401_HOME).state(NOT_STARTED).build()
        );

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
            Task.builder().event(FL401_HOME).state(NOT_STARTED).build()
        );

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
        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);
    }

    @Test
    public void testGetRespondentsEvents() {
        List<RespondentSolicitorEvents> actualRespEvents = taskListService.getRespondentsEvents();

        List<RespondentSolicitorEvents> expectedRespEvents = List.of(
            CONSENT,
            KEEP_DETAILS_PRIVATE,
            CONFIRM_EDIT_CONTACT_DETAILS,
            ATTENDING_THE_COURT,
            RespondentSolicitorEvents.MIAM,
            CURRENT_OR_PREVIOUS_PROCEEDINGS,
            RespondentSolicitorEvents.ALLEGATION_OF_HARM,
            RespondentSolicitorEvents.INTERNATIONAL_ELEMENT,
            ABILITY_TO_PARTICIPATE,
            VIEW_DRAFT_RESPONSE,
            RespondentSolicitorEvents.SUBMIT
        );
        assertThat(expectedRespEvents).isEqualTo(actualRespEvents);
    }
}

