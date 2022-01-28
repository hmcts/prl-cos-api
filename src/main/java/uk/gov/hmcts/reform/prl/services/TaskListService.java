package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.validators.EventsChecker;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.prl.enums.Event.*;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListService {

    private final EventsChecker eventsChecker;

    public List<Task> getTasksForOpenCase(CaseData caseData) {
        return getEvents(caseData).stream()
            .map(event -> Task.builder()
                .event(event)
                .state(getTaskState(caseData, event))
                .build())
            .collect(toList());
    }

    private TaskState getTaskState(CaseData caseData, Event event) {
        if (eventsChecker.isFinished(event, caseData)) {
            return TaskState.FINISHED;
        }
        if (eventsChecker.hasMandatoryCompleted(event, caseData)) {
            return TaskState.MANDATORY_COMPLETED;
        }
        if (eventsChecker.isStarted(event, caseData)) {
            return TaskState.IN_PROGRESS;
        }
        return TaskState.NOT_STARTED;
    }

    private List<Event> getEvents(CaseData caseData) {
        return caseData.getCaseTypeOfApplication().equalsIgnoreCase(PrlAppsConstants.FL401_CASE_TYPE)
            ? getFL401Events() : getC100Events();
    }

    private List<Event> getC100Events() {
        final List<Event> events = new ArrayList<>(List.of(
            CASE_NAME,
            TYPE_OF_APPLICATION,
            HEARING_URGENCY,
            APPLICANT_DETAILS,
            CHILD_DETAILS,
            RESPONDENT_DETAILS,
            MIAM,
            ALLEGATIONS_OF_HARM,
            OTHER_PEOPLE_IN_THE_CASE,
            OTHER_PROCEEDINGS,
            ATTENDING_THE_HEARING,
            INTERNATIONAL_ELEMENT,
            LITIGATION_CAPACITY,
            WELSH_LANGUAGE_REQUIREMENTS,
            VIEW_PDF_DOCUMENT,
            SUBMIT_AND_PAY
        ));

        return events;
    }

    private List<Event> getFL401Events() {
        final List<Event> events = new ArrayList<>(List.of(
            FL401_CASE_NAME,
            FL401_TYPE_OF_APPLICATION,
            APPLICANT_DETAILS,
            RESPONDENT_DETAILS,
            OTHER_PROCEEDINGS,
            INTERNATIONAL_ELEMENT,
            WELSH_LANGUAGE_REQUIREMENTS,
            VIEW_PDF_DOCUMENT));

        return events;
    }

}
