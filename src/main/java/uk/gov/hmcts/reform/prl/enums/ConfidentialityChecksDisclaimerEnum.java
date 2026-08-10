package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ConfidentialityChecksDisclaimerEnum {

    @CCD(label = "I have checked the application to ensure private information has not been disclosed.")
    confidentialityChecksChecked("I have checked the application "
                                     + "to ensure private information has not been disclosed.");

    private final String displayedValue;

}
