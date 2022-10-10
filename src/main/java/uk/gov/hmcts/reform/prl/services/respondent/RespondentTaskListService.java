package uk.gov.hmcts.reform.prl.services.respondent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import static uk.gov.hmcts.reform.prl.enums.Event.ABILITY_TO_PARICIPATE;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_COURT;
import static uk.gov.hmcts.reform.prl.enums.Event.CONSENT_TO_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.CURRENT_OR_PAST_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.EDIT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_MAIM;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_SUBMIT;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentTaskListService {

    private final EventsChecker eventsChecker;

    public List<Task> getTasksForRespondentOpenCase(CaseData caseData) {
        return getEvents(caseData).stream()
            .map(event -> Task.builder()
                .event(event)
                .state(getTaskState(caseData, event))
                .build())
            .collect(toList());
    }

    private TaskState getTaskState(CaseData caseData, Event event) {
        return TaskState.NOT_STARTED;
    }

    private List<Event> getEvents(CaseData caseData) {
        log.info("Case Data from respondent: ========================{}====================", caseData);
        return new ArrayList<>(List.of(
            CONSENT_TO_APPLICATION,
            KEEP_DETAILS_PRIVATE
        ));
    }
}
