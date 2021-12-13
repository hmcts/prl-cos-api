package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
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

}
