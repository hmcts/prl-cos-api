package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.abilitytoparticipate.LitigationCapacity;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.ABILITY_TO_PARTICIPATE_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.LITIGATION_CAPACITY;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AbilityToParticipateChecker implements RespondentEventChecker {
    private final RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            return ofNullable(response.get().getLitigationCapacity())
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
            Optional<LitigationCapacity> abilityToParticipate = Optional.ofNullable(
                response.get()
                    .getLitigationCapacity());
            if (!abilityToParticipate.isEmpty() && checkAbilityToParticipateMandatoryCompleted(abilityToParticipate)) {
                respondentTaskErrorService.removeError(ABILITY_TO_PARTICIPATE_ERROR);
                return true;
            }
        }
        respondentTaskErrorService.addEventError(
            LITIGATION_CAPACITY,
            ABILITY_TO_PARTICIPATE_ERROR,
            ABILITY_TO_PARTICIPATE_ERROR.getError()
        );
        return false;
    }

    private boolean checkAbilityToParticipateMandatoryCompleted(Optional<LitigationCapacity> abilityToParticipate) {

        List<Optional<?>> fields = new ArrayList<>();
        if (abilityToParticipate.isPresent()) {
            fields.add(ofNullable(abilityToParticipate.get().getFactorsAffectingAbilityToParticipate()));

            Optional<YesOrNo> abilityToParticipateYesOrNo = ofNullable(abilityToParticipate.get().getFactorsAffectingAbilityToParticipate());
            fields.add(abilityToParticipateYesOrNo);
            if (abilityToParticipateYesOrNo.isPresent() && YesNoDontKnow.yes.equals(abilityToParticipateYesOrNo.get())) {
                fields.add(ofNullable(abilityToParticipate.get().getProvideDetailsForFactorsAffectingAbilityToParticipate()));
            }
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
