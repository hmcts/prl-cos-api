package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GatekeeperEmail {
    @JsonProperty("email")
    private final String email;

    @JsonCreator
    public GatekeeperEmail(String email) {
        this.email  = email;
    }
}
