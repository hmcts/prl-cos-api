package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SdoNameOfRespondent {

    @JsonProperty("sdoNameOfRespondent")
    private final String sdoNameOfRespondent;

    @JsonCreator
    public SdoNameOfRespondent(String sdoNameOfRespondent) {
        this.sdoNameOfRespondent  = sdoNameOfRespondent;
    }
}
