package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum OrderTypeEnum {

    @JsonProperty("childArrangementsOrder")
    childArrangementsOrder("Child Arrangements Order"),
    @JsonProperty("prohibitedStepsOrder")
    prohibitedStepsOrder("Prohibited Steps Order"),
    @JsonProperty("specificIssueOrder")
    specificIssueOrder("Specific Issue Order");

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
