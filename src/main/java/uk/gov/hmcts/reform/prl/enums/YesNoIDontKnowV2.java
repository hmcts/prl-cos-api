package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum YesNoIDontKnowV2 {

    Yes("Yes"),
    No("No"),
    @CCD(label = "I don't know")
    IDontKnow("IDontKnow");

    private final String value;

    @JsonCreator
    public static YesNoIDontKnowV2 getValue(String key) {
        return YesNoIDontKnowV2.valueOf(key);
    }

    @JsonValue
    public String getDisplayedValue() {
        return value;
    }
}
