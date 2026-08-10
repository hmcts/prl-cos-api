package uk.gov.hmcts.reform.prl.enums.closingcase;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum CaseClosingReasonEnum {
    @CCD(label = "Application withdrawn")
    applicationWithdrawn("Application withdrawn"),
    @CCD(label = "Application refused")
    applicationRefused("Application refused"),
    @CCD(label = "No order made")
    noOrderMade("No order made"),
    @CCD(label = "Final order made")
    finalOrderMade("Final order made"),
    @CCD(label = "Consolidation")
    consolidation("Consolidation"),
    @CCD(label = "Housekeeping")
    housekeeping("Housekeeping");

    public String getDisplayedValue() {
        return displayedValue;
    }

    private final String displayedValue;
}
