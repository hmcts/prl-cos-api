package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class AllegationsOfHarmOverview {

    @CCD(label = "Are there allegations of harm?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo allegationsOfHarmYesNo;
    @CCD(label = "Any form of domestic abuse?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo allegationsOfHarmDomesticAbuseYesNo;
    @CCD(label = "Child abduction?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo allegationsOfHarmChildAbductionYesNo;
    @CCD(label = "Child abuse?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo allegationsOfHarmChildAbuseYesNo;
    @CCD(label = "Drugs alcohol or substance abuse?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo allegationsOfHarmSubstanceAbuseYesNo;
    @CCD(label = "Other safety or welfare concerns?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo allegationsOfHarmOtherConcernsYesNo;

}
