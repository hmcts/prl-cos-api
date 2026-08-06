package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "appointedGuardianFullName", generate = true)
@Data
@Builder
public class AppointedGuardianFullName {
    @CCD(label = "Full name", searchable = false)
    @JsonProperty("guardianFullName")
    private final String guardianFullName;

    @JsonCreator
    public AppointedGuardianFullName(String guardianFullName, @JsonProperty("guardianFullNameLabel") String guardianFullNameLabel) {
        this.guardianFullName  = guardianFullName;
        this.guardianFullNameLabel = guardianFullNameLabel;
    }

    /** Retained so existing positional call sites still compile. */
    public AppointedGuardianFullName(String guardianFullName) {
        this(guardianFullName, null);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String guardianFullNameLabel;
  // ==== end synthesised definition-only fields ====
}
