package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.EnumMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.enums.Event.*;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;

@Service
public class SubmitAndPayChecker implements EventChecker {

    @Autowired
    EventsChecker eventsChecker;

    @Override
    public boolean isFinished(CaseData caseData) {
        return hasMandatoryCompleted(caseData);
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return !hasMandatoryCompleted(caseData);
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {

        EnumMap<Event, EventChecker> mandatoryEvents = new EnumMap<Event, EventChecker>(Event.class);

        mandatoryEvents.put(CASE_NAME, eventsChecker.caseNameChecker);
        mandatoryEvents.put(TYPE_OF_APPLICATION, eventsChecker.applicationTypeChecker);
        mandatoryEvents.put(HEARING_URGENCY, eventsChecker.hearingUrgencyChecker);
        mandatoryEvents.put(APPLICANT_DETAILS, eventsChecker.applicantsChecker);
        mandatoryEvents.put(CHILD_DETAILS, eventsChecker.childChecker);
        mandatoryEvents.put(RESPONDENT_DETAILS, eventsChecker.respondentsChecker);
        mandatoryEvents.put(MIAM, eventsChecker.miamChecker);
        mandatoryEvents.put(ALLEGATIONS_OF_HARM, eventsChecker.allegationsOfHarmChecker);

        boolean mandatoryFinished = false;

        for (Map.Entry<Event, EventChecker> e : mandatoryEvents.entrySet()) {
            mandatoryFinished = e.getValue().isFinished(caseData);
        }

        EnumMap<Event, EventChecker> optionalEvents = new EnumMap<Event, EventChecker>(Event.class);

        optionalEvents.put(OTHER_PEOPLE_IN_THE_CASE, eventsChecker.otherPeopleInTheCaseChecker);
        optionalEvents.put(OTHER_PROCEEDINGS, eventsChecker.otherProceedingsChecker);
        optionalEvents.put(ATTENDING_THE_HEARING, eventsChecker.attendingTheHearingChecker);
        optionalEvents.put(INTERNATIONAL_ELEMENT, eventsChecker.internationalElementChecker);
        optionalEvents.put(LITIGATION_CAPACITY, eventsChecker.litigationCapacityChecker);
        optionalEvents.put(WELSH_LANGUAGE_REQUIREMENTS, eventsChecker.welshLanguageRequirementsChecker);

        boolean optionalFinished;

        for (Map.Entry<Event, EventChecker> e : optionalEvents.entrySet()) {
            optionalFinished = e.getValue().isFinished(caseData) || !(e.getValue().isStarted(caseData));
            if (!optionalFinished) {
                return false;
            }
        }

        return mandatoryFinished;
    }
}
