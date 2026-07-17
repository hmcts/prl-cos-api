package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Proceedings {
    @CCD(
            label = "order Type",
            searchable = false,
            typeOverride = FieldType.Text,
            typeParameterOverride = "TypeOfOrderEnum"
    )
    private final TypeOfOrderEnum orderType;
    @CCD(label = "Other proceedings ", searchable = false)
    @JsonProperty("proceedingDetails")
    private final List<Element<OtherProceedingDetails>> proceedingDetails;
}
