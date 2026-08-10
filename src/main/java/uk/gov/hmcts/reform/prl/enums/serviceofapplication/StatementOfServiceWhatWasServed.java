package uk.gov.hmcts.reform.prl.enums.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;


@ComplexType(name = "StatementOfServiceServedType", generate = true)
@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum StatementOfServiceWhatWasServed {

    @CCD(label = "Application pack")
    @JsonProperty("statementOfServiceApplicationPack")
    statementOfServiceApplicationPack("statementOfServiceApplicationPack", "Application pack"),
    @CCD(label = "Order")
    @JsonProperty("statementOfServiceOrder")
    statementOfServiceOrder("statementOfServiceOrder", "Order");

    private final String id;
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
