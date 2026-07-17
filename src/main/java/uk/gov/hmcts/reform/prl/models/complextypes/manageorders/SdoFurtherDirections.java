package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class SdoFurtherDirections {

    @CCD(label = "Title", searchable = false)
    @JsonProperty("sdoFurtherDirectionTitle")
    private final String sdoFurtherDirectionTitle;

    @CCD(label = "Description", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("sdoFurtherDirectionDescription")
    private final String sdoFurtherDirectionDescription;

    @JsonCreator
    public SdoFurtherDirections(String sdoFurtherDirectionTitle, String sdoFurtherDirectionDescription) {
        this.sdoFurtherDirectionTitle = sdoFurtherDirectionTitle;
        this.sdoFurtherDirectionDescription = sdoFurtherDirectionDescription;
    }
}
