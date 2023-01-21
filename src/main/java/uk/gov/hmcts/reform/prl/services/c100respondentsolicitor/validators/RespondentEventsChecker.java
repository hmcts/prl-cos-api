package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.EnumMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.CONSENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;

@Getter
@Service
public class RespondentEventsChecker {

    @Autowired
    private ConsentToApplicationChecker consentToApplicationChecker;

    @Autowired
    private KeepDetailsPrivateChecker keepDetailsPrivateChecker;


    private Map<RespondentSolicitorEvents, RespondentEventChecker> eventStatus = new EnumMap<>(RespondentSolicitorEvents.class);

    @PostConstruct
    public void init() {
        eventStatus.put(CONSENT, consentToApplicationChecker);
        eventStatus.put(KEEP_DETAILS_PRIVATE, keepDetailsPrivateChecker);

    }

    public boolean isStarted(RespondentSolicitorEvents event, CaseData caseData) {
        return eventStatus.get(event).isStarted(caseData);
    }

    public boolean hasMandatoryCompleted(RespondentSolicitorEvents event, CaseData caseData) {
        return eventStatus.get(event).hasMandatoryCompleted(caseData);
    }

    public Map<RespondentSolicitorEvents, RespondentEventChecker> getEventStatus() {
        return eventStatus;
    }
}
