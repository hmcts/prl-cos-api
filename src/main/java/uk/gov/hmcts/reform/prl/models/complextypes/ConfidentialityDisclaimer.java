package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ConfidentialityChecksDisclaimerEnum;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "confidentialityDisclaimerObject", generate = true)
@Data
@Builder
public class ConfidentialityDisclaimer {

    @CCD(label = " ", searchable = false)
    @JsonProperty("confidentialityChecksChecked")
    private final List<ConfidentialityChecksDisclaimerEnum> confidentialityChecksChecked;

    @JsonCreator
    public ConfidentialityDisclaimer(List<ConfidentialityChecksDisclaimerEnum> confidentialityChecksChecked, @JsonProperty("confidentialityStatementLabel") String confidentialityStatementLabel, @JsonProperty("confidentialityChecksLabel") String confidentialityChecksLabel, @JsonProperty("confidentialityChecksText") String confidentialityChecksText, @JsonProperty("confidentialityChecksTextResponse") String confidentialityChecksTextResponse) {
        this.confidentialityChecksChecked  = confidentialityChecksChecked;
        this.confidentialityStatementLabel = confidentialityStatementLabel;
        this.confidentialityChecksLabel = confidentialityChecksLabel;
        this.confidentialityChecksText = confidentialityChecksText;
        this.confidentialityChecksTextResponse = confidentialityChecksTextResponse;
    }

    /** Retained so existing positional call sites still compile. */
    public ConfidentialityDisclaimer(List<ConfidentialityChecksDisclaimerEnum> confidentialityChecksChecked) {
        this(confidentialityChecksChecked, null, null, null, null);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "# Confidentiality Statement", searchable = false, typeOverride = FieldType.Label)
  private String confidentialityStatementLabel;
  @CCD(label = "## Confidentiality checks", searchable = false, typeOverride = FieldType.Label)
  private String confidentialityChecksLabel;
  @CCD(
          label = "Ensure that no private information has been disclosed in the application.\n\nCheck:\n\n- any documents you complete now\n- any documents you complete in the future\n- documents received from other people, such as financial statements\n\nThe court staff will not be able to make these checks and will not be able to prevent any accidental disclosure of private information.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String confidentialityChecksText;
  @CCD(
          label = "Ensure that no private information has been disclosed in the response.\n\nCheck:\n\n- any documents you complete now\n- any documents you complete in the future\n- documents received from other people, such as financial statements\n\nThe court staff will not be able to make these checks and will not be able to prevent any accidental disclosure of private information.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String confidentialityChecksTextResponse;
  // ==== end synthesised definition-only fields ====
}
