package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConfidentialityChecksDisclaimerEnum {

    confidentialityChecksChecked("I have checked the application "
                                     + "to ensure private information has not been disclosed.");

    private final String displayedValue;

}
