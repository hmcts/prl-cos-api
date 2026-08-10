package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "withDrawApplicationObject", generate = true)
@Data
@Builder
@AllArgsConstructor
public class WithdrawApplication {
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo withDrawApplication;
    @CCD(label = "Why are you withdrawing the application?", searchable = false, typeOverride = FieldType.TextArea)
    private final String withDrawApplicationReason;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "## Are you sure you want to withdraw this application?",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String withDrawApplicationHeadingLabel;
  @CCD(
          label = "<div class='govuk-warning-text'><span class='govuk-warning-text__icon'>!</span><strong class='govuk-warning-text__text'>Once you have withdrawn this application you cannot resubmit it.</strong></div>",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String withDrawApplicationWarning;
  @CCD(
          label = "If you have paid a court fee, you will not get a refund.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String withDrawApplicationCourtInfo;
  // ==== end synthesised definition-only fields ====
}
