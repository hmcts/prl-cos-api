package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum C21OrderOptionsEnum {

    @CCD(label = "Blank order or directions (C21): application refused")
    @JsonProperty("c21ApplicationRefused")
    c21ApplicationRefused(
        "c21ApplicationRefused",
        "Blank order or directions (C21): application refused",
        "Gorchymyn gwag neu gyfarwyddiadau (C21): cais wedi’i wrthod",
        "C21 - General order or directions: application refused"
    ),

    @CCD(label = "Blank order or directions (C21): to withdraw application")
    @JsonProperty("c21WithdrawApplication")
    c21WithdrawApplication(
        "c21WithdrawApplication",
        "Blank order or directions (C21): to withdraw application",
        "Gorchymyn gwag neu gyfarwyddiadau (C21): tynnu cais yn ôl",
        "C21 - General order or directions: to withdraw application"
    ),

    @CCD(label = "Blank order or directions (C21): no order made")
    @JsonProperty("c21NoOrderMade")
    c21NoOrderMade(
        "c21NoOrderMade",
        "Blank order or directions (C21): no order made",
        "Gorchymyn gwag neu gyfarwyddiadau (C21): dim gorchymyn wedi’i wneud",
        "C21 - General order or directions: no order made"
    ),

    @CCD(label = "Blank order or directions (C21): Other")
    @JsonProperty("c21other")
    c21other(
        "c21other",
        "Blank order or directions (C21): Other",
        "Gorchymyn gwag neu gyfarwyddiadau (C21): Arall",
        "C21 - General order or directions"
    );

    private final String id;
    private final String displayedValue;
    private final String displayedValueWelsh;
    private final String headerValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    public String getDisplayedValueWelsh() {
        return displayedValueWelsh;
    }

    public String getHeaderValue() {
        return headerValue;
    }

    @JsonCreator
    public static C21OrderOptionsEnum getValue(String key) {
        return C21OrderOptionsEnum.valueOf(key);
    }
}
