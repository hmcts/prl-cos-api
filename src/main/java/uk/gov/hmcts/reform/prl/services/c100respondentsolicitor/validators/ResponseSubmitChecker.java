package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CURRENT_OR_PREVIOUS_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.MIAM;

@Slf4j
@SuppressWarnings("ALL")
@Service
public class ResponseSubmitChecker implements RespondentEventChecker {

    @Autowired
    @Lazy
    RespondentEventsChecker respondentEventsChecker;

    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        return false;
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        EnumMap<RespondentSolicitorEvents, RespondentEventChecker> mandatoryEvents = new EnumMap<>(RespondentSolicitorEvents.class);

        mandatoryEvents.put(CONSENT, respondentEventsChecker.getConsentToApplicationChecker());
        mandatoryEvents.put(KEEP_DETAILS_PRIVATE, respondentEventsChecker.getKeepDetailsPrivateChecker());
        mandatoryEvents.put(MIAM, respondentEventsChecker.getRespondentMiamChecker());
        mandatoryEvents.put(ABILITY_TO_PARTICIPATE, respondentEventsChecker.getAbilityToParticipateChecker());
        mandatoryEvents.put(ATTENDING_THE_COURT, respondentEventsChecker.getAttendToCourtChecker());
        mandatoryEvents.put(CURRENT_OR_PREVIOUS_PROCEEDINGS, respondentEventsChecker.getCurrentOrPastProceedingsChecker());
        mandatoryEvents.put(ALLEGATION_OF_HARM, respondentEventsChecker.getRespondentAllegationsOfHarmChecker());
        mandatoryEvents.put(CONFIRM_EDIT_CONTACT_DETAILS, respondentEventsChecker.getRespondentContactDetailsChecker());
        boolean mandatoryFinished;

        for (Map.Entry<RespondentSolicitorEvents, RespondentEventChecker> e : mandatoryEvents.entrySet()) {
            mandatoryFinished = e.getValue().isFinished(respondingParty);
            if (!mandatoryFinished) {
                return false;
            }
        }
        return true;
    }
}
