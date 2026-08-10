package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum HearingPriorityTypeEnum {

    @CCD(label = "Standard priority")
    @JsonProperty("StandardPriority")
    StandardPriority("StandardPriority","Standard priority"),
    @CCD(label = "Urgent priority")
    @JsonProperty("UrgentPriority")
    UrgentPriority("UrgentPriority","Urgent priority");

    private final String id;
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
