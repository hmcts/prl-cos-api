package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChildArrangementOrderTypeEnum {

    @JsonProperty("spendTimeWithOrder")
    SPEND_TIME_WITH_ORDER("spendTimeWithOrder", "Spend time with order"),
    @JsonProperty("liveWithOrder")
    LIVE_WITH_ORDER("liveWithOrder", "Live with order"),
    @JsonProperty("bothLiveWithAndSpendTimeWithOrder")
    BOTH_LIVE_WITH_AND_SPEND_TIME_WITH_ORDER("bothLiveWithAndSpendTimeWithOrder", "Both live with and spend time with order");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static OrderTypeEnum getValue(String key) {
        return OrderTypeEnum.valueOf(key);
    }

}
