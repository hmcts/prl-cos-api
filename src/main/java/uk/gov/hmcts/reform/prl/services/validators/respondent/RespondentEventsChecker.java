package uk.gov.hmcts.reform.prl.services.validators.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.validators.EventChecker;

import java.util.EnumMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import static uk.gov.hmcts.reform.prl.enums.Event.CONSENT_TO_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.KEEP_DETAILS_PRIVATE;

public class RespondentEventsChecker {

    @Autowired
    private ConsentToApplicationChecker consentToApplicationChecker;

    @Autowired
    private KeepDetailsPrivateChecker keepDetailsPrivateChecker;

    private Map<Event, EventChecker> eventStatus = new EnumMap<>(Event.class);

    @PostConstruct
    public void init() {

        eventStatus.put(CONSENT_TO_APPLICATION, consentToApplicationChecker);
        eventStatus.put(KEEP_DETAILS_PRIVATE, keepDetailsPrivateChecker);
    }

    public boolean isFinished(Event event, CaseData caseData) {
        return eventStatus.get(event).isFinished(caseData);
    }

    public boolean isStarted(Event event, CaseData caseData) {
        return eventStatus.get(event).isStarted(caseData);
    }

    public boolean hasMandatoryCompleted(Event event, CaseData caseData) {
        return eventStatus.get(event).hasMandatoryCompleted(caseData);
    }

    public Map<Event, EventChecker> getEventStatus() {
        return eventStatus;
    }
}
