package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class OtherConcerns {
    @CCD(label = " ", searchable = false)
    private final String c1AkeepingSafeStatement;
    @CCD(label = " ", searchable = false)
    private final String c1AsupervisionAgreementDetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo c1AagreementOtherWaysDetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo c1AotherConcernsDrugs;
    @CCD(label = " ", searchable = false)
    private final String c1AotherConcernsDrugsDetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo c1AchildSafetyConcerns;
    @CCD(label = " ", searchable = false)
    private final String c1AchildSafetyConcernsDetails;
}
