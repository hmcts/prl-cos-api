package uk.gov.hmcts.reform.prl.enums.reopenClosedCases;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ValidReopenClosedCasesStatusEnum {
    CASE_ISSUED("Case Issued"),
    PREPARE_FOR_HEARING_CONDUCT_HEARING("Hearing");

    public String getDisplayedValue() {
        return displayedValue;
    }

    private final String displayedValue;
}
