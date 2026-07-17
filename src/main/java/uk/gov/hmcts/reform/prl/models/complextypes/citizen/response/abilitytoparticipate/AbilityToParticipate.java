package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.abilitytoparticipate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class AbilityToParticipate {
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo factorsAffectingAbilityToParticipate;
    @CCD(label = " ", searchable = false)
    private final String provideDetailsForFactorsAffectingAbilityToParticipate;
    @CCD(label = " ", searchable = false)
    private final String giveDetailsAffectingLitigationCapacity;
    @CCD(label = " ", searchable = false)
    private final String detailsOfReferralOrAssessment;
}
