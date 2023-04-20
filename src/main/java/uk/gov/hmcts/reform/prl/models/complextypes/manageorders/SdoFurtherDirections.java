package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SdoFurtherDirections {

    @JsonProperty("sdoFurtherDirectionTitle")
    private final String sdoFurtherDirectionTitle;

    @JsonProperty("sdoFurtherDirectionDescription")
    private final String sdoFurtherDirectionDescription;

    @JsonCreator
    public SdoFurtherDirections(String sdoFurtherDirectionTitle, String sdoFurtherDirectionDescription) {
        this.sdoFurtherDirectionTitle = sdoFurtherDirectionTitle;
        this.sdoFurtherDirectionDescription = sdoFurtherDirectionDescription;
    }
}
