package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum MiamExemptionsChecklistEnum {

    @JsonProperty("domesticViolence")
    domesticViolence("Domestic violence"),
    @JsonProperty("urgency")
    urgency("Urgency"),
    @JsonProperty("previousMIAMattendance")
    previousMIAMattendance("Previous MIAM attendance or previous MIAM exemption"),
    @JsonProperty("other")
    other("Other");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static OrderTypeEnum getValue(String key) {
        return OrderTypeEnum.valueOf(key);
    }

}
