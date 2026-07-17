package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.BoldEnum;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder(toBuilder = true)
@Data
public class DirectionDetails {

    @CCD(label = " ", searchable = false)
    @JsonProperty("isBold")
    private final List<BoldEnum> isBold;
    @CCD(label = "Add directions and any further details", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("directionText")
    private final String directionText;

}
