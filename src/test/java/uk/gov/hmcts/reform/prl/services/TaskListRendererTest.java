package uk.gov.hmcts.reform.prl.services;

import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_HOME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_APPLICANT_FAMILY_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.prl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.LITIGATION_CAPACITY;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.RELATIONSHIP_TO_RESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_BEHAVIOUR;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.SUBMIT_AND_PAY;
import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.VIEW_PDF_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.WELSH_LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ALLEGATIONS_OF_HARM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ATTENDING_THE_HEARING_ERROR;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.FINISHED;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.IN_PROGRESS;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.NOT_STARTED;

public class TaskListRendererTest {

    private final TaskListRenderer taskListRenderer = new TaskListRenderer(
        new TaskListRenderElements(
            "NO IMAGE URL IN THIS BRANCH"
        )
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
        Task.builder().event(FL401_TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(RESPONDENT_BEHAVIOUR).state(NOT_STARTED).build(),
        Task.builder().event(FL401_APPLICANT_FAMILY_DETAILS).state(NOT_STARTED).build()
    );


    private final List<EventValidationErrors> errors = List.of(
        EventValidationErrors.builder().event(ALLEGATIONS_OF_HARM)
            .errors(Collections.singletonList(ALLEGATIONS_OF_HARM_ERROR.toString())).build(),
        EventValidationErrors.builder().event(ATTENDING_THE_HEARING)
            .errors(Collections.singletonList(ATTENDING_THE_HEARING_ERROR.toString())).build()
    );

    private final List<Task> fl401Tasks = List.of(
        Task.builder().event(FL401_CASE_NAME).state(NOT_STARTED).build(),
        Task.builder().event(FL401_TYPE_OF_APPLICATION).state(NOT_STARTED).build(),
        Task.builder().event(RESPONDENT_BEHAVIOUR).state(NOT_STARTED).build(),
        Task.builder().event(RELATIONSHIP_TO_RESPONDENT).state(NOT_STARTED).build(),
        Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(FL401_APPLICANT_FAMILY_DETAILS).state(NOT_STARTED).build(),
        Task.builder().event(OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
        Task.builder().event(ATTENDING_THE_HEARING).state(NOT_STARTED).build(),
        Task.builder().event(INTERNATIONAL_ELEMENT).state(NOT_STARTED).build(),
        Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
        Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build());

    @Test
    public void shouldRenderFl401TaskList() throws IOException {

        BufferedReader taskListMarkDown = new BufferedReader(new FileReader("src/test/resources/task-list-markdown.md"));

        List<String> lines = new ArrayList<>();

        String line = taskListMarkDown.readLine();
        while (line != null) {
            lines.add(line);
            line = taskListMarkDown.readLine();
        }

        String expectedTaskList = String.join("\n", lines);
        String actualTaskList = taskListRenderer.render(fl401Tasks, errors, false);

        Assert.assertNotEquals(expectedTaskList, actualTaskList);
        assertFalse(expectedTaskList.equals(actualTaskList));

    }

    @Test
    public void shouldRenderTaskList() throws IOException {

        BufferedReader taskListMarkDown = new BufferedReader(new FileReader("src/test/resources/task-list-markdown.md"));

        List<String> lines = new ArrayList<>();

        String line = taskListMarkDown.readLine();
        while (line != null) {
            lines.add(line);
            line = taskListMarkDown.readLine();
        }

        String expectedTaskList = String.join("\n", lines);
        String actualTaskList = taskListRenderer.render(tasks, errors, true);

        assertThat(expectedTaskList).isEqualTo(actualTaskList);
    }

    @Test
    public void shouldRenderTaskListWithNoErrors() throws IOException {
        List<EventValidationErrors> emptyErrors = Collections.emptyList();

        BufferedReader taskListMarkDown = new BufferedReader(new FileReader("src/test/resources/task-list-no-errors.md"));

        List<String> lines = new ArrayList<>();

        String line = taskListMarkDown.readLine();
        while (line != null) {
            lines.add(line);
            line = taskListMarkDown.readLine();
        }

        String expectedTaskList = String.join("\n", lines);
        String actualTaskList = taskListRenderer.render(tasks, emptyErrors, true);

        Assert.assertEquals(expectedTaskList, actualTaskList);
        assertTrue(expectedTaskList.equals(actualTaskList));

    }
}

