package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;

@Data
@Builder(toBuilder = true)
public class WithoutNoticeOrderDetails {
    @CCD(
            label = "Do you want to apply for the order without giving notice to the respondent",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("orderWithoutGivingNotice")
    private final YesOrNo orderWithoutGivingNotice;

    @JsonCreator
    public WithoutNoticeOrderDetails(YesOrNo orderWithoutGivingNotice, @JsonProperty("reasonForOrderWithoutGivingNotice") String reasonForOrderWithoutGivingNotice, @JsonProperty("futherDetails") String futherDetails, @JsonProperty("isRespondentAlreadyInBailCondition") YesNoDontKnow isRespondentAlreadyInBailCondition, @JsonProperty("bailConditionEndDate") java.time.LocalDate bailConditionEndDate, @JsonProperty("anyOtherDtailsForWithoutNoticeOrder") String anyOtherDtailsForWithoutNoticeOrder) {
        this.orderWithoutGivingNotice = orderWithoutGivingNotice;
        this.reasonForOrderWithoutGivingNotice = reasonForOrderWithoutGivingNotice;
        this.futherDetails = futherDetails;
        this.isRespondentAlreadyInBailCondition = isRespondentAlreadyInBailCondition;
        this.bailConditionEndDate = bailConditionEndDate;
        this.anyOtherDtailsForWithoutNoticeOrder = anyOtherDtailsForWithoutNoticeOrder;
    }

    /** Retained so existing positional call sites still compile. */
    public WithoutNoticeOrderDetails(YesOrNo orderWithoutGivingNotice) {
        this(orderWithoutGivingNotice, null, null, null, null, null);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Why do you want to apply without giving notice to the respondent?", searchable = false)
  private String reasonForOrderWithoutGivingNotice;
  @CCD(
          label = "Why do you think one or more of the reasons above may happen? (optional)",
          searchable = false,
          typeOverride = FieldType.TextArea
  )
  private String futherDetails;
  @CCD(label = "Is the respondent subject to any bail conditions?", searchable = false)
  private YesNoDontKnow isRespondentAlreadyInBailCondition;
  @CCD(label = "When do the bail conditions end?", searchable = false)
  private java.time.LocalDate bailConditionEndDate;
  @CCD(
          label = "Is there anything else about the applicant’s situation that you would like the court to know about, or consider?",
          searchable = false,
          typeOverride = FieldType.TextArea
  )
  private String anyOtherDtailsForWithoutNoticeOrder;
  // ==== end synthesised definition-only fields ====
}
