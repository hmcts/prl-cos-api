package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ChildArrangementOrderTypeEnum {

    @JsonProperty("spendTimeWithOrder")
    spendTimeWithOrder("spendTimeWithOrder", "Spend time with order"),
    @JsonProperty("liveWithOrder")
    liveWithOrder("liveWithOrder", "Live with order"),
    @JsonProperty("bothLiveWithAndSpendTimeWithOrder")
    bothLiveWithAndSpendTimeWithOrder("bothLiveWithAndSpendTimeWithOrder", "Both live with and spend time with order");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ChildArrangementOrderTypeEnum getValue(String key) {
        return ChildArrangementOrderTypeEnum.valueOf(key);
    }

}
