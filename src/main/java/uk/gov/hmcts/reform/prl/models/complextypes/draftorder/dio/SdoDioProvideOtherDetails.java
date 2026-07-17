package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class SdoDioProvideOtherDetails {

    @CCD(label = "Give details", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("sdoDioOtherDetails")
    private final String sdoDioOtherDetails;

    @JsonCreator
    public SdoDioProvideOtherDetails(String sdoDioOtherDetails) {
        this.sdoDioOtherDetails  = sdoDioOtherDetails;
    }
}
