package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderTypeEnum {

    childArrangementsOrder("Child Arrangements Order"),
    prohibitedStepsOrder("Prohibited Steps Order"),
    specificIssueOrder("specificIssueOrder");

    private final String displayedValue;

}
