package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoSection7ImpactAnalysisEnum {

    @JsonProperty("interimOrders")
    interimOrders("interimOrders", "For interim orders before determination of facts"),
    @JsonProperty("daOccured")
    daOccured("daOccured", "In all cases where domestic abuse has been found to have occurred");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoSection7ImpactAnalysisEnum getValue(String key) {
        return SdoSection7ImpactAnalysisEnum.valueOf(key);
    }
}
