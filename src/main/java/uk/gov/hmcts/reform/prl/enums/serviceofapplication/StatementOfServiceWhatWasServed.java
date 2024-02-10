package uk.gov.hmcts.reform.prl.enums.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;


@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum StatementOfServiceWhatWasServed {

    @JsonProperty("applicationPack")
    applicationPack("Application pack"),
    @JsonProperty("order")
    order("Order");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static StatementOfServiceWhatWasServed getValue(String key) {
        return StatementOfServiceWhatWasServed.valueOf(key);
    }

}
