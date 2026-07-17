package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class LocalCourtAdminEmail {
    @CCD(label = "*Email address", searchable = false, typeOverride = FieldType.Email)
    @JsonProperty("email")
    private final String email;

    @JsonCreator
    public LocalCourtAdminEmail(String email) {
        this.email  = email;
    }
}
