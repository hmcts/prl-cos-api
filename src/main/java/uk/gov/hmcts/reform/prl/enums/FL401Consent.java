package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum FL401Consent {

    @JsonProperty("fl401Consent")
    fl401Consent("The applicant believes that the facts stated in this form and any continuation "
                     + "sheets are true. I am authorised by the applicant to sign this statement. ");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static FL401Consent getValue(String key) {
        return FL401Consent.valueOf(key);
    }

    public static FL401Consent getDisplayedValueFromEnumString(String enteredValue) {
        if (enteredValue.equalsIgnoreCase("applicantConfirm")) {
            return FL401Consent.fl401Consent;
        } else if (enteredValue.equalsIgnoreCase("legalAidConfirm")) {
            return FL401Consent.fl401Consent;
        }
        return null;
    }
}
