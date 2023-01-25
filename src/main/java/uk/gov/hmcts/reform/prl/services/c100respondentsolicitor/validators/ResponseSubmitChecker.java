package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.EnumMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.ABILITY_TO_PARTICIPATE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.ATTENDING_THE_COURT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.CONSENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.CURRENT_OR_PREVIOUS_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.MIAM;

@Slf4j
@SuppressWarnings("ALL")
@Service
public class ResponseSubmitChecker implements RespondentEventChecker {

    @Autowired
    @Lazy
    RespondentEventsChecker respondentEventsChecker;

    @Override
    public boolean isStarted(CaseData caseData) {
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {

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
            mandatoryFinished = e.getValue().hasMandatoryCompleted(caseData);
            if (!mandatoryFinished) {
                return false;
            }
        }
        return true;
    }
}
