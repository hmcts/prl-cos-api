package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum ChildArrangementOrderTypeEnum {

    @JsonProperty("spendTimeWithOrder")
    spendTimeWithOrder("spendTimeWithOrder", "Spend time with order"),
    @JsonProperty("liveWithOrder")
    liveWithOrder("liveWithOrder", "Live with order"),
    @JsonProperty("bothLiveWithAndSpendTimeWithOrder")
    bothLiveWithAndSpendTimeWithOrder("bothLiveWithAndSpendTimeWithOrder", "Both live with and spend time with order");

    private final String id;
    private final String displayedValue;


}
