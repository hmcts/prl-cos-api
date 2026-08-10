package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;


@ComplexType(name = "MIAMExemptionsChecklistEnum", generate = true)
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamExemptionsChecklistEnum {

    @CCD(label = "Domestic violence ")
    @JsonProperty("domesticViolence")
    domesticViolence("Domestic violence"),
    @CCD(label = "Urgency")
    @JsonProperty("urgency")
    urgency("Urgency"),
    @CCD(label = "Previous MIAM attendance or previous MIAM exemption ")
    @JsonProperty("previousMIAMattendance")
    previousMIAMattendance("Previous MIAM attendance or previous MIAM exemption"),
    @CCD(label = "Other")
    @JsonProperty("other")
    other("Other"),
    @CCD(label = "Child protection concerns")
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
