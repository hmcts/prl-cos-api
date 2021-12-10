package uk.gov.hmcts.reform.prl.services;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.prl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.LITIGATION_CAPACITY;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.WELSH_LANGUAGE_REQUIREMENTS;

@RunWith(MockitoJUnitRunner.class)
public class TaskListServiceTest {

    @InjectMocks
    TaskListService taskListService;

    @Test
    public void getTasksShouldReturnListOfTasks() {

        CaseData caseData = CaseData.builder().build();

        List<Task> expectedTasks = List.of(
            Task.builder().event(CASE_NAME).build(),
            Task.builder().event(TYPE_OF_APPLICATION).build(),
            Task.builder().event(HEARING_URGENCY).build(),
            Task.builder().event(APPLICANT_DETAILS).build(),
            Task.builder().event(CHILD_DETAILS).build(),
            Task.builder().event(RESPONDENT_DETAILS).build(),
            Task.builder().event(MIAM).build(),
            Task.builder().event(ALLEGATIONS_OF_HARM).build(),
            Task.builder().event(OTHER_PEOPLE_IN_THE_CASE).build(),
            Task.builder().event(OTHER_PROCEEDINGS).build(),
            Task.builder().event(ATTENDING_THE_HEARING).build(),
            Task.builder().event(INTERNATIONAL_ELEMENT).build(),
            Task.builder().event(LITIGATION_CAPACITY).build(),
            Task.builder().event(WELSH_LANGUAGE_REQUIREMENTS).build()
        );

        List<Task> actualTasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(expectedTasks).isEqualTo(actualTasks);

    }

}
