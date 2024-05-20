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
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.MIAM;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.RESPOND_ALLEGATION_OF_HARM;


@Slf4j
@Service
@SuppressWarnings({"java:S6813"})
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
        mandatoryEvents.put(ATTENDING_THE_COURT, respondentEventsChecker.getAttendToCourtChecker());
        mandatoryEvents.put(ALLEGATION_OF_HARM, respondentEventsChecker.getRespondentAllegationsOfHarmChecker());
        mandatoryEvents.put(CONFIRM_EDIT_CONTACT_DETAILS, respondentEventsChecker.getRespondentContactDetailsChecker());
        mandatoryEvents.put(RESPOND_ALLEGATION_OF_HARM, respondentEventsChecker.getResponseToAllegationsOfHarmChecker());
        boolean mandatoryFinished;
        EnumMap<RespondentSolicitorEvents, RespondentEventChecker> optionalEvents = new EnumMap<>(RespondentSolicitorEvents.class);
        optionalEvents.put(OTHER_PROCEEDINGS, respondentEventsChecker.getCurrentOrPastProceedingsChecker());
        optionalEvents.put(INTERNATIONAL_ELEMENT,respondentEventsChecker.getInternationalElementsChecker());
        optionalEvents.put(ABILITY_TO_PARTICIPATE, respondentEventsChecker.getAbilityToParticipateChecker());

        boolean optionalFinished;

        for (Map.Entry<RespondentSolicitorEvents, RespondentEventChecker> e : mandatoryEvents.entrySet()) {
            log.info("mandatory events" );
            mandatoryFinished = e.getValue().isFinished(respondingParty);
            log.info("event {} mandatoryFinished {}", e.getKey(), mandatoryFinished);
            if (!mandatoryFinished) {
                log.info("returning false");
                return false;
            }
        }
        for (Map.Entry<RespondentSolicitorEvents, RespondentEventChecker> e : optionalEvents.entrySet()) {
            log.info("optional events");
            optionalFinished = e.getValue().isFinished(respondingParty) || !(e.getValue().isStarted(respondingParty));
            log.info("event {} optionalFinished {}", e.getKey(), optionalFinished);
            if (!optionalFinished) {
                log.info("returning false");
                return false;
            }
        }
        log.info("returning true");
        return true;
    }
}
