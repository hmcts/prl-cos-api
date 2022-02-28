package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FL401ConsentEnum {

    @JsonProperty("fl401Consent")
    fl401Consent("The applicant believes that the facts stated in this "
                     + "form and any continuation sheets are true. I am authorised by the "
                     + "applicant to sign this statement.");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static FL401ConsentEnum getValue(String key) {
        return FL401ConsentEnum.valueOf(key);
    }
}
