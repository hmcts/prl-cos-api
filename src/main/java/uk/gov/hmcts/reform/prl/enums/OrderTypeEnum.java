package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum OrderTypeEnum {

    @CCD(label = "Child Arrangements Order")
    @JsonProperty("childArrangementsOrder")
    childArrangementsOrder("Child Arrangements Order"),
    @CCD(label = "Prohibited Steps Order")
    @JsonProperty("prohibitedStepsOrder")
    prohibitedStepsOrder("Prohibited Steps Order"),
    @CCD(label = "Specific Issue Order")
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
