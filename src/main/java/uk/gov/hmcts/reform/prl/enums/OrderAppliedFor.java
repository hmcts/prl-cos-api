package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderAppliedFor {

    childArrangementsOrder("Child Arrangements Order");

    private final String displayedValue;

}
