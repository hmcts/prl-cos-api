package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.abilitytoparticipate.AbilityToParticipate;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.ABILITY_TO_PARTICIPATE_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ABILITY_TO_PARTICIPATE;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class AbilityToParticipateChecker implements RespondentEventChecker {
    @Autowired
    RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            return ofNullable(response.get().getAbilityToParticipate())
                .filter(ability -> anyNonEmpty(
                    ability.getFactorsAffectingAbilityToParticipate(),
                    ability.getProvideDetailsForFactorsAffectingAbilityToParticipate()
                )).isPresent();
        }
        return false;
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {

        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            Optional<AbilityToParticipate> abilityToParticipate = Optional.ofNullable(
                response.get()
                    .getAbilityToParticipate());
            if (!abilityToParticipate.isEmpty() && checkAbilityToParticipateMandatoryCompleted(abilityToParticipate)) {
                respondentTaskErrorService.removeError(ABILITY_TO_PARTICIPATE_ERROR);
                return true;
            }
        }
        respondentTaskErrorService.addEventError(
            ABILITY_TO_PARTICIPATE,
            ABILITY_TO_PARTICIPATE_ERROR,
            ABILITY_TO_PARTICIPATE_ERROR.getError()
        );
        return false;
    }

    private boolean checkAbilityToParticipateMandatoryCompleted(Optional<AbilityToParticipate> abilityToParticipate) {

        List<Optional<?>> fields = new ArrayList<>();
        if (abilityToParticipate.isPresent()) {
            fields.add(ofNullable(abilityToParticipate.get().getFactorsAffectingAbilityToParticipate()));

            Optional<YesNoDontKnow> abilityToParticipateYesOrNo = ofNullable(abilityToParticipate.get().getFactorsAffectingAbilityToParticipate());
            fields.add(abilityToParticipateYesOrNo);
            if (abilityToParticipateYesOrNo.isPresent() && YesNoDontKnow.yes.equals(abilityToParticipateYesOrNo.get())) {
                fields.add(ofNullable(abilityToParticipate.get().getProvideDetailsForFactorsAffectingAbilityToParticipate()));
            }
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
