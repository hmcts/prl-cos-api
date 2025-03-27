package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum C21OrderOptionsEnum {

    @JsonProperty("c21ApplicationRefused")
    c21ApplicationRefused("c21ApplicationRefused", "Blank order or directions (C21): application refused",
        "Gorchymyn gwag neu gyfarwyddiadau (C21): cais wedi’i wrthod"),
    @JsonProperty("c21WithdrawApplication")
    c21WithdrawApplication("c21WithdrawApplication", "Blank order or directions (C21): to withdraw application",
        "Gorchymyn gwag neu gyfarwyddiadau (C21): tynnu cais yn ôl"),
    @JsonProperty("c21NoOrderMade")
    c21NoOrderMade("c21NoOrderMade", "Blank order or directions (C21): no order made",
        "Gorchymyn gwag neu gyfarwyddiadau (C21): dim gorchymyn wedi’i wneud"),
    @JsonProperty("c21other")
    c21other("c21other", "Blank order or directions (C21): Other",
        "Gorchymyn gwag neu gyfarwyddiadau (C21): Arall");

    private final String id;
    private final String displayedValue;
    private final String displayedValueWelsh;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    public String getDisplayedValueWelsh() {
        return displayedValueWelsh;
    }

    @JsonCreator
    public static C21OrderOptionsEnum getValue(String key) {
        return C21OrderOptionsEnum.valueOf(key);
    }
}
