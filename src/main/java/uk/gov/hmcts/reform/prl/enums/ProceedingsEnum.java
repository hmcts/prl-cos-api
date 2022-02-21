package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProceedingsEnum {

    @JsonProperty("ongoing")
    ongoing("ongoing", "Ongoing"),
    @JsonProperty("previous")
    previous("previous", "Previous");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ProceedingsEnum getValue(String key) {
        return ProceedingsEnum.valueOf(key);
    }

}
