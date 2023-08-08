package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum HearingSpecificDatesOptionsEnum {

    @JsonProperty("Yes")
    Yes("Yes", "Yes"),
    @JsonProperty("No")
    No("No", "No"),
    @JsonProperty("HearingRequiredBetweenCertainDates")
    HearingRequiredBetweenCertainDates("HearingRequiredBetweenCertainDates", "It needs to take place between certain dates");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static HearingSpecificDatesOptionsEnum getValue(String key) {
        return HearingSpecificDatesOptionsEnum.valueOf(key);
    }

}
