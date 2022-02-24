package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.EnumMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.enums.Event.*;

@Service
public class FL401StatementOfTruthAndSubmitChecker implements EventChecker {

    @Autowired
    EventsChecker eventsChecker;

    @Override
    public boolean isFinished(CaseData caseData) {

        return hasMandatoryCompleted(caseData);
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        EnumMap<Event, EventChecker> mandatoryEvents = new EnumMap<Event, EventChecker>(Event.class);

        mandatoryEvents.put(FL401_CASE_NAME, eventsChecker.caseNameChecker);
        mandatoryEvents.put(FL401_TYPE_OF_APPLICATION, eventsChecker.fl401ApplicationTypeChecker);
        mandatoryEvents.put(WITHOUT_NOTICE_ORDER, eventsChecker.withoutNoticeOrderChecker);
        mandatoryEvents.put(APPLICANT_DETAILS, eventsChecker.applicantsChecker);
        mandatoryEvents.put(RESPONDENT_DETAILS, eventsChecker.respondentsChecker);
        mandatoryEvents.put(RELATIONSHIP_TO_RESPONDENT, eventsChecker.respondentRelationshipChecker);
        mandatoryEvents.put(FL401_APPLICANT_FAMILY_DETAILS, eventsChecker.fl401ApplicantFamilyChecker);
        mandatoryEvents.put(RESPONDENT_BEHAVIOUR, eventsChecker.respondentBehaviourChecker);

        boolean mandatoryFinished;

        for (Map.Entry<Event, EventChecker> e : mandatoryEvents.entrySet()) {
            mandatoryFinished = e.getValue().isFinished(caseData);
            if (!mandatoryFinished) {
                return false;
            }
        }

        EnumMap<Event, EventChecker> optionalEvents = new EnumMap<Event, EventChecker>(Event.class);

        optionalEvents.put(OTHER_PROCEEDINGS, eventsChecker.otherProceedingsChecker);
        optionalEvents.put(ATTENDING_THE_HEARING, eventsChecker.attendingTheHearingChecker);
        optionalEvents.put(WELSH_LANGUAGE_REQUIREMENTS, eventsChecker.welshLanguageRequirementsChecker);

        boolean optionalFinished;

        for (Map.Entry<Event, EventChecker> e : optionalEvents.entrySet()) {
            optionalFinished = e.getValue().isFinished(caseData) || !(e.getValue().isStarted(caseData));
            if (!optionalFinished) {
                return false;
            }
        }

        return true;
    }
}
