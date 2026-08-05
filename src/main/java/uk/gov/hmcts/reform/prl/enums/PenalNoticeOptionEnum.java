package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum PenalNoticeOptionEnum {

    @JsonProperty("Yes")
    Yes("Yes, a Penal Notice is needed");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static PenalNoticeOptionEnum getValue(String key) {
        return PenalNoticeOptionEnum.valueOf(key);
    }

}
