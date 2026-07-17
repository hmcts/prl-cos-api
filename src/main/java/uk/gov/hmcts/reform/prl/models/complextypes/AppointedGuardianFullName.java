package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder
public class AppointedGuardianFullName {
    @CCD(label = "Full name", searchable = false)
    @JsonProperty("guardianFullName")
    private final String guardianFullName;

    @JsonCreator
    public AppointedGuardianFullName(String guardianFullName) {
        this.guardianFullName  = guardianFullName;
    }
}
