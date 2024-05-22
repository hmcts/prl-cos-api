package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM_REVISED;
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
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ALLEGATIONS_OF_HARM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ATTENDING_THE_HEARING_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.FL401_APPLICANT_FAMILY_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.WITHOUT_NOTICE_ORDER_ERROR;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.FINISHED;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.IN_PROGRESS;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.NOT_STARTED;


public class TaskListRendererTest {
    private TypeOfApplicationOrders orders;
    private LinkToCA linkToCA;
    private EventsChecker eventsChecker = Mockito.mock(EventsChecker.class);


    private final TaskListRenderer taskListRenderer = new TaskListRenderer(
        new TaskListRenderElements(
            "NO IMAGE URL IN THIS BRANCH"
        ),eventsChecker
    );

    private final List<Task> tasks = List.of(
        Task.builder().event(CASE_NAME).state(NOT_STARTED).state(NOT_STARTED).build(),
        Task.builder().event(TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(HEARING_URGENCY).state(NOT_STARTED).build(),
        Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(CHILD_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(MIAM).state(NOT_STARTED).build(),
        Task.builder().event(ALLEGATIONS_OF_HARM).state(IN_PROGRESS).build(),
        Task.builder().event(ALLEGATIONS_OF_HARM_REVISED).state(IN_PROGRESS).build(),
        Task.builder().event(OTHER_PEOPLE_IN_THE_CASE).state(NOT_STARTED).build(),
        Task.builder().event(OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
        Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
        Task.builder().event(INTERNATIONAL_ELEMENT).state(FINISHED).build(),
        Task.builder().event(LITIGATION_CAPACITY).state(FINISHED).build(),
        Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
        Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
        Task.builder().event(FL401_HOME).state(NOT_STARTED).build(),
        Task.builder().event(SUBMIT_AND_PAY).state(NOT_STARTED).build(),
        Task.builder().event(FL401_CASE_NAME).state(NOT_STARTED).build(),
        Task.builder().event(WITHOUT_NOTICE_ORDER).state(NOT_STARTED).build(),
        Task.builder().event(FL401_TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(RESPONDENT_BEHAVIOUR).state(NOT_STARTED).build(),
        Task.builder().event(FL401_APPLICANT_FAMILY_DETAILS).state(NOT_STARTED).build());

    private final List<Task> tasksResubmit = List.of(
        Task.builder().event(CASE_NAME).state(NOT_STARTED).state(NOT_STARTED).build(),
        Task.builder().event(TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(HEARING_URGENCY).state(NOT_STARTED).build(),
        Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(CHILD_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(MIAM).state(NOT_STARTED).build(),
        Task.builder().event(ALLEGATIONS_OF_HARM).state(IN_PROGRESS).build(),
        Task.builder().event(ALLEGATIONS_OF_HARM_REVISED).state(IN_PROGRESS).build(),
        Task.builder().event(OTHER_PEOPLE_IN_THE_CASE).state(NOT_STARTED).build(),
        Task.builder().event(OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
        Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
        Task.builder().event(INTERNATIONAL_ELEMENT).state(FINISHED).build(),
        Task.builder().event(LITIGATION_CAPACITY).state(FINISHED).build(),
        Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
        Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
        Task.builder().event(FL401_HOME).state(NOT_STARTED).build(),
        Task.builder().event(SUBMIT).state(NOT_STARTED).build(),
        Task.builder().event(FL401_CASE_NAME).state(NOT_STARTED).build(),
        Task.builder().event(WITHOUT_NOTICE_ORDER).state(NOT_STARTED).build(),
        Task.builder().event(FL401_TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(RESPONDENT_BEHAVIOUR).state(NOT_STARTED).build(),
        Task.builder().event(FL401_APPLICANT_FAMILY_DETAILS).state(NOT_STARTED).build());


    private final List<Task> taskC100V2 = List.of(
            Task.builder().event(CASE_NAME).state(NOT_STARTED).state(NOT_STARTED).build(),
            Task.builder().event(TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
            Task.builder().event(HEARING_URGENCY).state(NOT_STARTED).build(),
            Task.builder().event(CHILD_DETAILS_REVISED).state(NOT_STARTED).build(),
            Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
            Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
            Task.builder().event(OTHER_PEOPLE_IN_THE_CASE_REVISED).state(NOT_STARTED).build(),
            Task.builder().event(OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION).state(NOT_STARTED).build(),
            Task.builder().event(CHILDREN_AND_APPLICANTS).state(NOT_STARTED).build(),
            Task.builder().event(CHILDREN_AND_RESPONDENTS).state(NOT_STARTED).build(),
            Task.builder().event(CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION).state(NOT_STARTED).build(),
            Task.builder().event(ALLEGATIONS_OF_HARM_REVISED).state(IN_PROGRESS).build(),
            Task.builder().event(MIAM).state(NOT_STARTED).build(),
            Task.builder().event(OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
            Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
            Task.builder().event(INTERNATIONAL_ELEMENT).state(FINISHED).build(),
            Task.builder().event(LITIGATION_CAPACITY).state(FINISHED).build(),
            Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
            Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
            Task.builder().event(SUBMIT_AND_PAY).state(NOT_STARTED).build());

    private final List<Task> taskC100V2Resubmission = List.of(
        Task.builder().event(CASE_NAME).state(NOT_STARTED).state(NOT_STARTED).build(),
        Task.builder().event(TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(HEARING_URGENCY).state(NOT_STARTED).build(),
        Task.builder().event(CHILD_DETAILS_REVISED).state(NOT_STARTED).build(),
        Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(OTHER_PEOPLE_IN_THE_CASE_REVISED).state(NOT_STARTED).build(),
        Task.builder().event(OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(CHILDREN_AND_APPLICANTS).state(NOT_STARTED).build(),
        Task.builder().event(CHILDREN_AND_RESPONDENTS).state(NOT_STARTED).build(),
        Task.builder().event(CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(ALLEGATIONS_OF_HARM_REVISED).state(IN_PROGRESS).build(),
        Task.builder().event(MIAM).state(NOT_STARTED).build(),
        Task.builder().event(OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
        Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
        Task.builder().event(INTERNATIONAL_ELEMENT).state(FINISHED).build(),
        Task.builder().event(LITIGATION_CAPACITY).state(FINISHED).build(),
        Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
        Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
        Task.builder().event(SUBMIT).state(NOT_STARTED).build());

    private final List<Task> taskC100V3 = List.of(
        Task.builder().event(CASE_NAME).state(NOT_STARTED).state(NOT_STARTED).build(),
        Task.builder().event(TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(HEARING_URGENCY).state(NOT_STARTED).build(),
        Task.builder().event(CHILD_DETAILS_REVISED).state(NOT_STARTED).build(),
        Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(OTHER_PEOPLE_IN_THE_CASE_REVISED).state(NOT_STARTED).build(),
        Task.builder().event(OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(CHILDREN_AND_APPLICANTS).state(NOT_STARTED).build(),
        Task.builder().event(CHILDREN_AND_RESPONDENTS).state(NOT_STARTED).build(),
        Task.builder().event(CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(ALLEGATIONS_OF_HARM_REVISED).state(IN_PROGRESS).build(),
        Task.builder().event(MIAM_POLICY_UPGRADE).state(NOT_STARTED).build(),
        Task.builder().event(OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
        Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
        Task.builder().event(INTERNATIONAL_ELEMENT).state(FINISHED).build(),
        Task.builder().event(LITIGATION_CAPACITY).state(FINISHED).build(),
        Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
        Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
        Task.builder().event(SUBMIT_AND_PAY).state(NOT_STARTED).build());

    private final List<Task> taskC100V3Resubmitted = List.of(
        Task.builder().event(CASE_NAME).state(NOT_STARTED).state(NOT_STARTED).build(),
        Task.builder().event(TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(HEARING_URGENCY).state(NOT_STARTED).build(),
        Task.builder().event(CHILD_DETAILS_REVISED).state(NOT_STARTED).build(),
        Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(OTHER_PEOPLE_IN_THE_CASE_REVISED).state(NOT_STARTED).build(),
        Task.builder().event(OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(CHILDREN_AND_APPLICANTS).state(NOT_STARTED).build(),
        Task.builder().event(CHILDREN_AND_RESPONDENTS).state(NOT_STARTED).build(),
        Task.builder().event(CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(ALLEGATIONS_OF_HARM_REVISED).state(IN_PROGRESS).build(),
        Task.builder().event(MIAM_POLICY_UPGRADE).state(NOT_STARTED).build(),
        Task.builder().event(OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
        Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
        Task.builder().event(INTERNATIONAL_ELEMENT).state(FINISHED).build(),
        Task.builder().event(LITIGATION_CAPACITY).state(FINISHED).build(),
        Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
        Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
        Task.builder().event(SUBMIT).state(NOT_STARTED).build());


    private final List<EventValidationErrors> errors = List.of(
        EventValidationErrors.builder().event(ALLEGATIONS_OF_HARM)
            .errors(Collections.singletonList(ALLEGATIONS_OF_HARM_ERROR.toString())).build(),
        EventValidationErrors.builder().event(ATTENDING_THE_HEARING)
            .errors(Collections.singletonList(ATTENDING_THE_HEARING_ERROR.toString())).build()
    );

    private final List<Task> fl401Tasks = List.of(
        Task.builder().event(FL401_CASE_NAME).state(NOT_STARTED).build(),
        Task.builder().event(WITHOUT_NOTICE_ORDER).state(NOT_STARTED).build(),
        Task.builder().event(FL401_TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(RELATIONSHIP_TO_RESPONDENT).state(NOT_STARTED).build(),
        Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(FL401_APPLICANT_FAMILY_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(FL401_OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
        Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
        Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
        Task.builder().event(FL401_UPLOAD_DOCUMENTS).state(NOT_STARTED).build(),
        Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
        Task.builder().event(FL401_SOT_AND_SUBMIT).state(NOT_STARTED).build(),
        Task.builder().event(FL401_HOME).state(NOT_STARTED).build(),
        Task.builder().event(RESPONDENT_BEHAVIOUR).state(NOT_STARTED).build()
    );

    private final List<Task> fl401TasksResubmit = List.of(
        Task.builder().event(FL401_CASE_NAME).state(NOT_STARTED).build(),
        Task.builder().event(WITHOUT_NOTICE_ORDER).state(NOT_STARTED).build(),
        Task.builder().event(FL401_TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(RELATIONSHIP_TO_RESPONDENT).state(NOT_STARTED).build(),
        Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(FL401_APPLICANT_FAMILY_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(FL401_OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
        Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
        Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
        Task.builder().event(FL401_UPLOAD_DOCUMENTS).state(NOT_STARTED).build(),
        Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build(),
        Task.builder().event(FL401_RESUBMIT).state(NOT_STARTED).build(),
        Task.builder().event(FL401_HOME).state(NOT_STARTED).build(),
        Task.builder().event(RESPONDENT_BEHAVIOUR).state(NOT_STARTED).build()
    );

    private final List<EventValidationErrors> fl401Errors = List.of(
        EventValidationErrors.builder().event(WITHOUT_NOTICE_ORDER)
            .errors(Collections.singletonList(WITHOUT_NOTICE_ORDER_ERROR.toString())).build(),
        EventValidationErrors.builder().event(FL401_APPLICANT_FAMILY_DETAILS)
            .errors(Collections.singletonList(FL401_APPLICANT_FAMILY_ERROR.toString())).build()
    );

    @Test
    public void shouldRenderFl401TaskList() throws IOException {

        BufferedReader taskListMarkDown = new BufferedReader(new FileReader(
            "src/test/resources/fl401-task-list-markdown.md"));

        List<String> lines = new ArrayList<>();

        String line = taskListMarkDown.readLine();
        while (line != null) {
            lines.add(line);
            line = taskListMarkDown.readLine();
        }

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
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .build();

        String expectedTaskList = String.join("\n", lines);
        String actualTaskList = taskListRenderer.render(fl401Tasks, fl401Errors, false, caseData);

        assertNotEquals(expectedTaskList, actualTaskList);
    }

    @Test
    public void shouldRenderTaskList() throws IOException {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        List<String> lines = new ArrayList<>();

        BufferedReader taskListMarkDown = new BufferedReader(new FileReader("src/test/resources/task-list-markdown.md"));

        String line = taskListMarkDown.readLine();
        while (line != null) {
            lines.add(line);
            line = taskListMarkDown.readLine();
        }

        String expectedTaskList = String.join("\n", lines);
        String actualTaskList = taskListRenderer.render(tasks, errors, true, caseData);

        assertThat(expectedTaskList).isEqualTo(actualTaskList);
    }

    @Test
    public void shouldRenderTaskListIfResubmitted() throws IOException {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_RESUBMISSION_TO_HMCTS)
            .build();

        String actualTaskList = taskListRenderer.render(tasksResubmit, errors, true, caseData);

        assertNotNull(actualTaskList);
    }

    @Test
    public void shouldRenderTaskListWithNoErrors() throws IOException {
        List<EventValidationErrors> emptyErrors = Collections.emptyList();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        List<String> lines = new ArrayList<>();

        BufferedReader taskListMarkDown = new BufferedReader(new FileReader("src/test/resources/task-list-no-errors.md"));

        String line = taskListMarkDown.readLine();
        while (line != null) {
            lines.add(line);
            line = taskListMarkDown.readLine();
        }

        String expectedTaskList = String.join("\n", lines);
        String actualTaskList = taskListRenderer.render(tasks, emptyErrors, true, caseData);

        assertEquals(expectedTaskList, actualTaskList);

    }

    @Test
    public void shouldRenderTaskListWithAllegationOfHarmRevisedNoErrors() throws IOException {
        List<EventValidationErrors> emptyErrors = Collections.emptyList();

        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS).taskListVersion(TASK_LIST_VERSION_V2)
                .build();

        List<String> lines = new ArrayList<>();

        BufferedReader taskListMarkDown = new BufferedReader(new FileReader("src/test/resources/task-list-allegations-revised-no-errors.md"));

        String line = taskListMarkDown.readLine();
        while (line != null) {
            lines.add(line);
            line = taskListMarkDown.readLine();
        }

        String expectedTaskList = String.join("\n", lines);
        String actualTaskList = taskListRenderer.render(taskC100V2, emptyErrors, true, caseData);

        assertEquals(expectedTaskList, actualTaskList);

    }

    @Test
    public void shouldRenderFl401TaskListNonMolestationOrderType() throws IOException {

        BufferedReader taskListMarkDown = new BufferedReader(new FileReader("src/test/resources/fl401-task-list-markdown.md"));

        List<String> lines = new ArrayList<>();

        String line = taskListMarkDown.readLine();
        while (line != null) {
            lines.add(line);
            line = taskListMarkDown.readLine();
        }

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
            .typeOfApplicationLinkToCA(linkToCA)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        String expectedTaskList = String.join("\n", lines);
        String actualTaskList = taskListRenderer.render(fl401Tasks, fl401Errors, false, caseData);

        assertNotEquals(expectedTaskList, actualTaskList);
    }

    @Test
    public void shouldRenderFl401TaskWithResubmit() throws IOException {

        BufferedReader taskListMarkDown = new BufferedReader(new FileReader("src/test/resources/fl401-task-list-resubmit.md"));

        List<String> lines = new ArrayList<>();

        String line = taskListMarkDown.readLine();
        while (line != null) {
            lines.add(line);
            line = taskListMarkDown.readLine();
        }

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
            .typeOfApplicationLinkToCA(linkToCA)
            .state(State.AWAITING_RESUBMISSION_TO_HMCTS)
            .build();

        String expectedTaskList = String.join("\n", lines);
        String actualTaskList = taskListRenderer.render(fl401TasksResubmit, fl401Errors, false, caseData);

        assertEquals(expectedTaskList, actualTaskList);
    }

    @Test
    public void shouldRenderC100V2TaskList() throws IOException {
        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .taskListVersion(TASK_LIST_VERSION_V2)
                .build();
        List<String> lines = new ArrayList<>();

        BufferedReader taskListMarkDown = new BufferedReader(new FileReader("src/test/resources/task-list-markdown-v2.md"));

        String line = taskListMarkDown.readLine();
        while (line != null) {
            lines.add(line);
            line = taskListMarkDown.readLine();
        }

        String expectedTaskList = String.join("\n", lines);
        String actualTaskList = taskListRenderer.render(taskC100V2, errors, true, caseData);

        assertThat(expectedTaskList).isEqualTo(actualTaskList);
    }

    @Test
    public void shouldRenderC100V2TaskListResubmission() throws IOException {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_RESUBMISSION_TO_HMCTS)
            .taskListVersion(TASK_LIST_VERSION_V2)
            .build();

        String actualTaskList = taskListRenderer.render(taskC100V2Resubmission, errors, true, caseData);

        assertNotNull(actualTaskList);
    }

    @Test
    public void shouldRenderC100V3TaskList() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .taskListVersion(TASK_LIST_VERSION_V3)
            .build();
        String actualTaskList = taskListRenderer.render(taskC100V3, errors, true, caseData);

        assertNotNull(actualTaskList);
    }

    @Test
    public void shouldRenderC100V3TaskListResubmitted() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_RESUBMISSION_TO_HMCTS)
            .taskListVersion(TASK_LIST_VERSION_V3)
            .build();
        String actualTaskList = taskListRenderer.render(taskC100V3Resubmitted, errors, true, caseData);

        assertNotNull(actualTaskList);
    }
}
