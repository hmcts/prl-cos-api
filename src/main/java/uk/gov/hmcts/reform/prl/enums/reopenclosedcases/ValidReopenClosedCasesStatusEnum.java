package uk.gov.hmcts.reform.prl.enums.reopenclosedcases;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum ValidReopenClosedCasesStatusEnum {
    @CCD(label = "Case Issued")
    CASE_ISSUED("Case Issued"),
    @CCD(label = "Hearing")
    PREPARE_FOR_HEARING_CONDUCT_HEARING("Hearing");

    public String getDisplayedValue() {
        return displayedValue;
    }

    private final String displayedValue;
}
