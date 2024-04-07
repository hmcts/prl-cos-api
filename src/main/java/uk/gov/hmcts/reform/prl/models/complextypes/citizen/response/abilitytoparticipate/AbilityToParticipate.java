package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.abilitytoparticipate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class AbilityToParticipate {
    private final YesOrNo factorsAffectingAbilityToParticipate;
    private final String provideDetailsForFactorsAffectingAbilityToParticipate;
    private final String giveDetailsAffectingLitigationCapacity;
    private final String detailsOfReferralOrAssessment;
}
