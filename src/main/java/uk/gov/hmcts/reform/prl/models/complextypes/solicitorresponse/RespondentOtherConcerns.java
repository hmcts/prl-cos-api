package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class RespondentOtherConcerns {

    @CCD(
            label = "*What steps or orders does the respondent want the court to take or make to protect the safety of the child(ren) and/or themselves?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String ordersRespondentWantFromCourt;
    @CCD(
            label = "*Do you agree to the child(ren) spending unsupervised time with the other person(s) in receipt of this form?\n ",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo childSpendingUnsupervisedTime;
    @CCD(
            label = "*Do you agree to the child(ren) spending supervised time with the other person(s) in receipt of this form?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo childSpendingSupervisedTime;
    @CCD(
            label = "*Do you agree to the child having other forms of contact with the other person in receipt of this form? (by telephone, text, email, social media)",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo childHavingOtherFormOfContact;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Child contact", searchable = false, typeOverride = FieldType.Label)
  private String childContactLabel;
  // ==== end synthesised definition-only fields ====
}
