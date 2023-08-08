package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.util.EnumMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ABILITY_TO_PARTICIPATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ATTENDING_THE_COURT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONSENT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CURRENT_OR_PREVIOUS_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.MIAM;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.VIEW_DRAFT_RESPONSE;

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

    @Autowired
    private ViewDraftResponseChecker viewDraftResponseChecker;

    @Autowired
    private ResponseSubmitChecker responseSubmitChecker;

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
        eventStatus.put(INTERNATIONAL_ELEMENT, internationalElementsChecker);
        eventStatus.put(CONFIRM_EDIT_CONTACT_DETAILS, respondentContactDetailsChecker);
        eventStatus.put(VIEW_DRAFT_RESPONSE, viewDraftResponseChecker);
        eventStatus.put(SUBMIT, responseSubmitChecker);
    }

    public boolean isStarted(RespondentSolicitorEvents event, PartyDetails respondingParty) {
        return eventStatus.get(event).isStarted(respondingParty);
    }

    public boolean isFinished(RespondentSolicitorEvents event, PartyDetails respondingParty) {
        return eventStatus.get(event).isFinished(respondingParty);
    }

    public Map<RespondentSolicitorEvents, RespondentEventChecker> getEventStatus() {
        return eventStatus;
    }
}
