package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class AllegationsOfHarmRevisedChildContact {

    @CCD(
            label = "Do you agree to the child(ren) spending unsupervised time with the other person(s) in receipt of this form?\n",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo newAgreeChildUnsupervisedTime;
    @CCD(
            label = "Do you agree to the child(ren) spending supervised time with the other person(s) in receipt of this form?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo newAgreeChildSupervisedTime;
    @CCD(
            label = "Do you agree to the child having other forms of contact with the other person in receipt of this form? (by telephone, text, email, social media)",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo newAgreeChildOtherContact;


}
