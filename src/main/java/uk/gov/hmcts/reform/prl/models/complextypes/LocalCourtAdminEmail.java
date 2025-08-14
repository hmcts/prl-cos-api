package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocalCourtAdminEmail {
    @JsonProperty("email")
    private final String email;

    @JsonCreator
    public LocalCourtAdminEmail(String email) {
        this.email  = email;
    }
}
