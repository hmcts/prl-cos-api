package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class AllegationsOfHarmRevisedOverview {
    @CCD(label = "Are there allegations of harm?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo newAllegationsOfHarmYesNo;
    @CCD(
            label = "Any form of domestic abuse towards the respondent",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo newAllegationsOfHarmDomesticAbuseYesNo;
    @CCD(label = "Child abduction", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo newAllegationsOfHarmChildAbductionYesNo;
    @CCD(
            label = "Child abuse towards the children in this application",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo newAllegationsOfHarmChildAbuseYesNo;
    @CCD(label = "Drugs, alcohol or substance abuse", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo newAllegationsOfHarmSubstanceAbuseYesNo;
    @CCD(label = "Give details", searchable = false, typeOverride = FieldType.TextArea)
    private final String newAllegationsOfHarmSubstanceAbuseDetails;
    @CCD(label = "Other safety or welfare concerns", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo newAllegationsOfHarmOtherConcerns;
    @CCD(label = "Give details", searchable = false, typeOverride = FieldType.TextArea)
    private final String newAllegationsOfHarmOtherConcernsDetails;

}
