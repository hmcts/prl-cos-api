package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorAbilityToParticipateInProceedings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class AbilityToParticipateChecker implements RespondentEventChecker {
    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        return response
            .filter(res -> anyNonEmpty(res.getAbilityToParticipate()
            )).isPresent();
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        boolean mandatoryInfo = false;

        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            Optional<SolicitorAbilityToParticipateInProceedings> abilityToParticipate = Optional.ofNullable(
                response.get()
                    .getAbilityToParticipate());
            if (!abilityToParticipate.isEmpty() && checkAbilityToParticipateMandatoryCompleted(abilityToParticipate)) {
                mandatoryInfo = true;
            }
        }
        return mandatoryInfo;
    }

    private boolean checkAbilityToParticipateMandatoryCompleted(Optional<SolicitorAbilityToParticipateInProceedings> abilityToParticipate) {

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
