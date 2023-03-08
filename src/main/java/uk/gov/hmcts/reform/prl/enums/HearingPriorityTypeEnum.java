package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum HearingPriorityTypeEnum {

    @JsonProperty("StandardPriority")
    StandardPriority("StandardPriority"),
    @JsonProperty("UrgentPriority")
    UrgentPriority("UrgentPriority");


    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static HearingPriorityTypeEnum getValue(String key) {
        return HearingPriorityTypeEnum.valueOf(key);
    }
}
