package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.validators.EventsChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_APPLICANT_FAMILY_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_HOME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_OTHER_PROCEEDINGS;
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
            ? getFL401Events(caseData) : getC100Events();
    }

    public List<Event> getC100Events() {
        return new ArrayList<>(List.of(
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
            SUBMIT_AND_PAY,
            SUBMIT
        ));

    }

    public List<Event> getFL401Events(CaseData caseData) {

        Optional<TypeOfApplicationOrders> ordersOptional = ofNullable(caseData.getTypeOfApplicationOrders());

        List<Event> eventsList = new ArrayList<>(List.of(
            FL401_CASE_NAME,
            FL401_TYPE_OF_APPLICATION,
            WITHOUT_NOTICE_ORDER,
            APPLICANT_DETAILS,
            RESPONDENT_DETAILS,
            FL401_APPLICANT_FAMILY_DETAILS,
            RELATIONSHIP_TO_RESPONDENT,
            FL401_OTHER_PROCEEDINGS,
            ATTENDING_THE_HEARING,
            WELSH_LANGUAGE_REQUIREMENTS,
            FL401_UPLOAD_DOCUMENTS,
            VIEW_PDF_DOCUMENT,
            FL401_SOT_AND_SUBMIT
        ));

        if (ordersOptional.isEmpty() || (ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)
            &&
            ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder))) {
            eventsList.add(RESPONDENT_BEHAVIOUR);
            eventsList.add(FL401_HOME);
        } else  if (ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)) {
            eventsList.add(FL401_HOME);
        } else if (ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder)) {
            eventsList.add(RESPONDENT_BEHAVIOUR);
        }
        return eventsList;
    }
}
