package uk.gov.hmcts.reform.prl.enums.closingcase;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseClosingReasonEnum {
    applicationWithdrawn("Application withdrawn"),
    applicationRefused("Application refused"),
    noOrderMade("No order made"),
    finalOrderMade("Final order made"),
    consolidation("Consolidation"),
    housekeeping("Housekeeping");

    public String getDisplayedValue() {
        return displayedValue;
    }

    private final String displayedValue;
}
