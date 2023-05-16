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
    c21ApplicationRefused("c21ApplicationRefused", "Blank order or directions (C21): application refused"),
    @JsonProperty("c21WithdrawApplication")
    c21WithdrawApplication("c21WithdrawApplication", "Blank order or directions (C21): to withdraw application"),
    @JsonProperty("c21NoOrderMade")
    c21NoOrderMade("c21NoOrderMade", "Blank order or directions (C21): no order made"),
    @JsonProperty("c21other")
    c21other("c21other", "Blank order or directions (C21): Other");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static C21OrderOptionsEnum getValue(String key) {
        return C21OrderOptionsEnum.valueOf(key);
    }
}
