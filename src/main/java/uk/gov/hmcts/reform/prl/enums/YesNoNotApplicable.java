package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum YesNoNotApplicable {

    @JsonProperty("Yes")
    Yes("Yes"),

    @JsonProperty("No")
    No("No"),

    @CCD(label = "Not applicable")
    @JsonProperty("NotApplicable")
    NotApplicable("NotApplicable");

    private final String value;

    @JsonCreator
    public static YesNoNotApplicable getValue(String key) {
        return YesNoNotApplicable.valueOf(key);
    }

    @JsonValue
    public String getDisplayedValue() {
        return value;
    }
}
