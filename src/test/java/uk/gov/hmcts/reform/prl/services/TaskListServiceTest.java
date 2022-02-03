package uk.gov.hmcts.reform.prl.services;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.services.validators.EventsChecker;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.prl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.LITIGATION_CAPACITY;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.SUBMIT_AND_PAY;
import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.VIEW_PDF_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.WELSH_LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.NOT_STARTED;

@RunWith(MockitoJUnitRunner.class)
public class TaskListServiceTest {

    @InjectMocks
    TaskListService taskListService;

    @Mock
    EventsChecker eventsChecker;

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
            Task.builder().event(SUBMIT_AND_PAY).state(NOT_STARTED).build());

        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);

    }

    @Test
    public void getTasksShouldReturnFl401ListOfTasks() {

        CaseData caseData = CaseData.builder().caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE).build();

        List<Task> expectedTasks = List.of(
            Task.builder().event(FL401_CASE_NAME).state(NOT_STARTED).build(),
            Task.builder().event(APPLICANT_DETAILS).state(NOT_STARTED).build(),
            Task.builder().event(RESPONDENT_DETAILS).state(NOT_STARTED).build(),
            Task.builder().event(OTHER_PROCEEDINGS).state(NOT_STARTED).build(),
            Task.builder().event(INTERNATIONAL_ELEMENT).state(NOT_STARTED).build(),
            Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).state(NOT_STARTED).build(),
            Task.builder().event(VIEW_PDF_DOCUMENT).state(NOT_STARTED).build());

        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);

    }

}

