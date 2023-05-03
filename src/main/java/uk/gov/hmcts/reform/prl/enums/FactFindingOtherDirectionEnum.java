package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum FactFindingOtherDirectionEnum {
    @JsonProperty("other")
    other("other", "Other direction for the 'directions for fact-finding' hearing");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static FactFindingOtherDirectionEnum getValue(String key) {
        return FactFindingOtherDirectionEnum.valueOf(key);
    }
}
