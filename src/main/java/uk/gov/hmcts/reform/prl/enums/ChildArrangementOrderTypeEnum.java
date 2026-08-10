package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "childArrangementOrderTypeEnum", generate = true)
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ChildArrangementOrderTypeEnum {

    @CCD(label = "Spend time with order")
    @JsonProperty("spendTimeWithOrder")
    spendTimeWithOrder("spendTimeWithOrder", "Spend time with order"),
    @CCD(label = "Live with order")
    @JsonProperty("liveWithOrder")
    liveWithOrder("liveWithOrder", "Live with order"),
    @CCD(label = "Both live with and spend time with order")
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
