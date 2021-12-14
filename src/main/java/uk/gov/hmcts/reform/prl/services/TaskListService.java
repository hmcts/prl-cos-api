package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.validators.EventsChecker;


import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.prl.enums.Event.*;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListService {

    private final EventsChecker eventsChecker;
//    private final FeatureToggleService featureToggles;

    public List<Task> getTasksForOpenCase(CaseData caseData) {
        return getEvents(caseData).stream()
            .map(event -> Task.builder()
                .event(event)
                .state(getTaskState(caseData, event))
                .build())
            .collect(toList());
    }

    //this is hashed together slightly to deal with the submit and pay event.

    private TaskState getTaskState(CaseData caseData, Event event) {
        if (eventsChecker.isFinished(event, caseData)) {
            return TaskState.FINISHED;
        }
        if (eventsChecker.hasMandatoryCompleted(event, caseData)) {
            return TaskState.MANDATORY_COMPLETED;
        }
        if (eventsChecker.isStarted(event, caseData)) {
            if (event.equals(SUBMIT_AND_PAY)) {
                return TaskState.NOT_STARTED;
            }
            return TaskState.IN_PROGRESS;
        }
        return TaskState.NOT_STARTED;
    }

    private List<Event> getEvents(CaseData caseData) {

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

}
