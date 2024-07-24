package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.util.EnumMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ABILITY_TO_PARTICIPATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ATTENDING_THE_COURT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONSENT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.MIAM;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.RESPOND_ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.VIEW_DRAFT_RESPONSE;

@Getter
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentEventsChecker {
    private final ConsentToApplicationChecker consentToApplicationChecker;
    private final KeepDetailsPrivateChecker keepDetailsPrivateChecker;
    private final RespondentMiamChecker respondentMiamChecker;
    private final AbilityToParticipateChecker abilityToParticipateChecker;
    private final AttendToCourtChecker attendToCourtChecker;
    private final CurrentOrPastProceedingsChecker currentOrPastProceedingsChecker;
    private final InternationalElementsChecker internationalElementsChecker;
    private final RespondentContactDetailsChecker respondentContactDetailsChecker;
    private final RespondentAllegationsOfHarmChecker respondentAllegationsOfHarmChecker;
    private final ViewDraftResponseChecker viewDraftResponseChecker;
    private final ResponseSubmitChecker responseSubmitChecker;
    private final ResponseToAllegationsOfHarmChecker responseToAllegationsOfHarmChecker;

    private Map<RespondentSolicitorEvents, RespondentEventChecker> eventStatus = new EnumMap<>(RespondentSolicitorEvents.class);

    @PostConstruct
    public void init() {
        eventStatus.put(CONSENT, consentToApplicationChecker);
        eventStatus.put(KEEP_DETAILS_PRIVATE, keepDetailsPrivateChecker);
        eventStatus.put(ABILITY_TO_PARTICIPATE, abilityToParticipateChecker);
        eventStatus.put(ATTENDING_THE_COURT, attendToCourtChecker);
        eventStatus.put(MIAM, respondentMiamChecker);
        eventStatus.put(OTHER_PROCEEDINGS, currentOrPastProceedingsChecker);
        eventStatus.put(ALLEGATION_OF_HARM, respondentAllegationsOfHarmChecker);
        eventStatus.put(INTERNATIONAL_ELEMENT, internationalElementsChecker);
        eventStatus.put(CONFIRM_EDIT_CONTACT_DETAILS, respondentContactDetailsChecker);
        eventStatus.put(VIEW_DRAFT_RESPONSE, viewDraftResponseChecker);
        eventStatus.put(SUBMIT, responseSubmitChecker);
        eventStatus.put(RESPOND_ALLEGATION_OF_HARM, responseToAllegationsOfHarmChecker);
    }

    public boolean isStarted(RespondentSolicitorEvents event, PartyDetails respondingParty, boolean isC1aApplicable) {
        return eventStatus.get(event).isStarted(respondingParty, isC1aApplicable);
    }

    public boolean isFinished(RespondentSolicitorEvents event, PartyDetails respondingParty, boolean isC1aApplicable) {
        return eventStatus.get(event).isFinished(respondingParty, isC1aApplicable);
    }

    public Map<RespondentSolicitorEvents, RespondentEventChecker> getEventStatus() {
        return eventStatus;
    }
}
