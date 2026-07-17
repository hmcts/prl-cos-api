package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
public class SdoNameOfApplicant {

    @CCD(label = "Name of applicant", searchable = false)
    @JsonProperty("sdoNameOfApplicant")
    private final String sdoNameOfApplicant;

    @JsonCreator
    public SdoNameOfApplicant(String sdoNameOfApplicant) {
        this.sdoNameOfApplicant  = sdoNameOfApplicant;
    }
}
