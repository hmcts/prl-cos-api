package uk.gov.hmcts.reform.prl.enums.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum AllocateOrReserveJudgeEnum {

    @JsonProperty("allocatedTo")
    allocatedTo("allocatedTo", "Allocated to"),
    @JsonProperty("reservedTo")
    reservedTo("reservedTo", "Reserved to");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static AllocateOrReserveJudgeEnum getValue(String key) {
        return AllocateOrReserveJudgeEnum.valueOf(key);
    }

}
