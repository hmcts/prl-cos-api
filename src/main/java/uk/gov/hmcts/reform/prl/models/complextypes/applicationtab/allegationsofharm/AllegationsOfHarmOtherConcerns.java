package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class AllegationsOfHarmOtherConcerns {

    @CCD(
            label = "Are there other concerns about the child(ren)’s safety and wellbeing?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo allegationsOfHarmOtherConcerns;
    @CCD(label = "*Give details", searchable = false, typeOverride = FieldType.TextArea)
    private final String allegationsOfHarmOtherConcernsDetails;
    @CCD(
            label = "What steps or orders does the applicant want the court to take or make to protect the safety of the child(ren) and/or yourself?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String allegationsOfHarmOtherConcernsCourtActions;
    @CCD(
            label = "*Do you agree to the child(ren) spending unsupervised time with the other person(s) in receipt of this form?\n",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo agreeChildUnsupervisedTime;
    @CCD(
            label = "*Do you agree to the child(ren) spending supervised time with the other person(s) in receipt of this form?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo agreeChildSupervisedTime;
    @CCD(
            label = "*Do you agree to the child having other forms of contact with the other person in receipt of this form? (by telephone, text, email, social media)",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo agreeChildOtherContact;


}
