package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.UrgencyTimeFrameType;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Builder
@Data
public class Urgency {
    @CCD(label = "Status", searchable = false)
    private final String urgencyStatus;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Urgency", searchable = false)
  private UrgencyTimeFrameType urgencyType;
  @CCD(label = "Reason entered for urgent", searchable = false)
  private String urgencyReason;
  // ==== end synthesised definition-only fields ====
}
