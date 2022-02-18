package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderTypeEnum {

    @JsonProperty("childArrangementsOrder")
    childArrangementsOrder("Child Arrangements Order"),
    @JsonProperty("prohibitedStepsOrder")
    prohibitedStepsOrder("Prohibited Steps Order"),
    @JsonProperty("specificIssueOrder")
    specificIssueOrder("Specific Issue Order");

    private final String displayedValue;
}
