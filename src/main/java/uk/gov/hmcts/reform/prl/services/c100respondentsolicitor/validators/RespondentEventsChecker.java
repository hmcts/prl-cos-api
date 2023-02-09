package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.EnumMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.ABILITY_TO_PARTICIPATE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.ATTENDING_THE_COURT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.CONSENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.CURRENT_OR_PREVIOUS_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.MIAM;

@Getter
@Service
public class RespondentEventsChecker {

    @Autowired
    private ConsentToApplicationChecker consentToApplicationChecker;

    @Autowired
    private KeepDetailsPrivateChecker keepDetailsPrivateChecker;

    @Autowired
    private RespondentMiamChecker respondentMiamChecker;

    @Autowired
    private AbilityToParticipateChecker abilityToParticipateChecker;

    @Autowired
    private AttendToCourtChecker attendToCourtChecker;

    @Autowired
    private CurrentOrPastProceedingsChecker currentOrPastProceedingsChecker;

    @Autowired
    private InternationalElementsChecker internationalElementsChecker;

    @Autowired
    private RespondentContactDetailsChecker respondentContactDetailsChecker;

    @Autowired
    private RespondentAllegationsOfHarmChecker respondentAllegationsOfHarmChecker;

    private Map<RespondentSolicitorEvents, RespondentEventChecker> eventStatus = new EnumMap<>(RespondentSolicitorEvents.class);

    @PostConstruct
    public void init() {
        eventStatus.put(CONSENT, consentToApplicationChecker);
        eventStatus.put(KEEP_DETAILS_PRIVATE, keepDetailsPrivateChecker);
        eventStatus.put(ABILITY_TO_PARTICIPATE, abilityToParticipateChecker);
        eventStatus.put(ATTENDING_THE_COURT, attendToCourtChecker);
        eventStatus.put(MIAM, respondentMiamChecker);
        eventStatus.put(CURRENT_OR_PREVIOUS_PROCEEDINGS, currentOrPastProceedingsChecker);
        eventStatus.put(ALLEGATION_OF_HARM, respondentAllegationsOfHarmChecker);
        eventStatus.put(INTERNATIONAL_ELEMENT, getInternationalElementsChecker());
        eventStatus.put(CONFIRM_EDIT_CONTACT_DETAILS, respondentContactDetailsChecker);

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
