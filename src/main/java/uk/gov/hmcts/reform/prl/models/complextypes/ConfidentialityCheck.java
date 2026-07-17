package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Fl401ConfidentialConsentEnum;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Fl401ConfidentialConsent;

@Data
@Builder
public class ConfidentialityCheck {
    @CCD(ignore = true)
    private final Fl401ConfidentialConsentEnum fl401ConfidentialConsentEnum;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Ensure that no confidential information has been disclosed in the application.\nCheck:\n\n- any documents you complete now\n- any documents you complete in the future\n- documents received from other people, such as financial statements\n\nThe court staff will not be able to make these checks and will not be able to prevent any accidental disclosure of confidential information.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String confidentialityLabel;
  @CCD(label = " ", searchable = false)
  private java.util.Set<Fl401ConfidentialConsent> confidentialityConsent;
  // ==== end synthesised definition-only fields ====
}
