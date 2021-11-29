package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
//import uk.gov.hmcts.reform.prl.enums.TaskState;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;


import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.prl.enums.Event.*;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListService {

//    private final EventsChecker eventsChecker;
//    private final FeatureToggleService featureToggles;

    public List<Task> getTasksForOpenCase(CaseData caseData) {
        return getEvents(caseData).stream()
            .map(event -> Task.builder()
                .event(event)
                //.state(getTaskState(caseData, event))
                .build())
            .collect(toList());
    }

//    private TaskState getTaskState(CaseData caseData, Event event) {
//        if (eventsChecker.isCompleted(event, caseData)) {
//            return eventsChecker.completedState(event);
//        }
//
//        if (eventsChecker.isInProgress(event, caseData)) {
//            return IN_PROGRESS;
//        }
//
//        if (!eventsChecker.isAvailable(event, caseData)) {
//            return NOT_AVAILABLE;
//        }
//
//        return NOT_STARTED;
//    }

    private List<Event> getEvents(CaseData caseData) {

        final List<Event> events = new ArrayList<>(List.of(
            MIAM,
            ATTENDING_THE_HEARING,
            HEARING_URGENCY,
            APPLICANT_DETAILS,
            CHILD_DETAILS,
            RESPONDENT_DETAILS,
            TYPE_OF_APPLICATION,
            CASE_NAME,
            ALLEGATIONS_OF_HARM
        ));

        return events;
    }

}
