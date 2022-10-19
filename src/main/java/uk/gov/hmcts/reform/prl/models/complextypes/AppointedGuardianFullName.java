package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointedGuardianFullName {
    @JsonProperty("guardianFullName")
    private final String guardianFullName;

    @JsonCreator
    public AppointedGuardianFullName(String guardianFullName) {
        this.guardianFullName  = guardianFullName;
    }
}
