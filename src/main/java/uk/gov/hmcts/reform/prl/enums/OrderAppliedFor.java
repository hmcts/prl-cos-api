package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderAppliedFor {

    childArrangementsOrder("Child Arrangements Order"),
    prohibitedStepsOrder("Prohibited Steps Order"),
    specificIssueOrder("specificIssueOrder");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static OrderAppliedFor getValue(String key) {
        return OrderAppliedFor.valueOf(key);
    }

}
