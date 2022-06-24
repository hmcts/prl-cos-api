package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamExemptionsChecklistEnum {

    @JsonProperty("domesticViolence")
    domesticViolence("Domestic violence"),
    @JsonProperty("urgency")
    urgency("Urgency"),
    @JsonProperty("previousMIAMattendance")
    previousMIAMattendance("Previous MIAM attendance or previous MIAM exemption"),
    @JsonProperty("other")
    other("Other"),
    @JsonProperty("childProtectionConcern")
    childProtectionConcern("Child Protection Concern");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MiamExemptionsChecklistEnum getValue(String key) {
        return MiamExemptionsChecklistEnum.valueOf(key);
    }

}
