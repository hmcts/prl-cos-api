package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SdoNameOfApplicant {

    @JsonProperty("sdoNameOfApplicant")
    private final String sdoNameOfApplicant;

    @JsonCreator
    public SdoNameOfApplicant(String sdoNameOfApplicant) {
        this.sdoNameOfApplicant  = sdoNameOfApplicant;
    }
}
