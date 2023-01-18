package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class SolicitorAbilityToParticipateInProceedings {

    private final YesNoDontKnow factorsAffectingAbilityToParticipate;
    private final String provideDetailsForFactorsAffectingAbilityToParticipate;
}
