package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum WithDrawTypeOfOrderEnum {

    @JsonProperty("withdrawnApplication")
    withdrawnApplication("withdrawnApplication", "Withdrawn application"),
    @JsonProperty("refusedApplication")
    refusedApplication("refusedApplication", "Refused application"),
    @JsonProperty("noOrderMade")
    noOrderMade("noOrderMade", "No order made");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static WithDrawTypeOfOrderEnum getValue(String key) {
        return WithDrawTypeOfOrderEnum.valueOf(key);
    }

}
