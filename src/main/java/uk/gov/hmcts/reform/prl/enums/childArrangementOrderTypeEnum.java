package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum childArrangementOrderTypeEnum {

    SPEND_TIME_WITH_ORDER("spendTimeWithOrder", "Spend time with order"),
    LIVE_WITH_ORDER("liveWithOrder", "Live with order"),
    BOTH_LIVE_WITH_AND_SPEND_TIME_WITH_ORDER("bothLiveWithAndSpendTimeWithOrder", "Both live with and spend time with order");

    private final String id;
    private final String displayedValue;

}
