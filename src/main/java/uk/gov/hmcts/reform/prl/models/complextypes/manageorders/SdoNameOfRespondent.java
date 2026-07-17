package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
public class SdoNameOfRespondent {

    @CCD(label = "Name of respondent", searchable = false)
    @JsonProperty("sdoNameOfRespondent")
    private final String sdoNameOfRespondent;

    @JsonCreator
    public SdoNameOfRespondent(String sdoNameOfRespondent) {
        this.sdoNameOfRespondent  = sdoNameOfRespondent;
    }
}
