package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
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

}
