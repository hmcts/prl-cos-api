package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ConfidentialityChecksDisclaimerEnum {

    confidentialityChecksChecked("I have checked the application "
                                     + "to ensure private information has not been disclosed.");

    private final String displayedValue;

}
