package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "localCourtAdminEmail", generate = true)
@Data
@Builder
public class LocalCourtAdminEmail {
    @CCD(label = "*Email address", searchable = false, typeOverride = FieldType.Email)
    @JsonProperty("email")
    private final String email;

    @JsonCreator
    public LocalCourtAdminEmail(String email, @JsonProperty("addNewEmailLabel") String addNewEmailLabel) {
        this.email  = email;
        this.addNewEmailLabel = addNewEmailLabel;
    }

    /** Retained so existing positional call sites still compile. */
    public LocalCourtAdminEmail(String email) {
        this(email, null);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String addNewEmailLabel;
  // ==== end synthesised definition-only fields ====
}
