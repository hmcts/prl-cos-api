package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Fl401ConfidentialConsentEnum {

    @JsonProperty("fl401ConfidentialConsent")
    fl401ConfidentialConsent("I have checked the application to "
                                 + "ensure confidential information has not been disclosed.");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static Fl401ConfidentialConsentEnum getValue(String key) {
        return Fl401ConfidentialConsentEnum.valueOf(key);
    }
}
