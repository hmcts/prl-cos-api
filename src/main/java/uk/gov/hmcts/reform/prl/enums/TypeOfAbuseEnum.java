package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum TypeOfAbuseEnum {

    @JsonProperty("TypeOfAbuseEnum_value_1")
    TypeOfAbuseEnum_value_1("Physical abuse"),

    @JsonProperty("TypeOfAbuseEnum_value_2")
    TypeOfAbuseEnum_value_2("Psychological abuse"),

    @JsonProperty("TypeOfAbuseEnum_value_3")
    TypeOfAbuseEnum_value_3("Sexual abuse"),

    @JsonProperty("TypeOfAbuseEnum_value_4")
    TypeOfAbuseEnum_value_4("Emotional abuse"),

    @JsonProperty("TypeOfAbuseEnum_value_5")
    TypeOfAbuseEnum_value_5("Financial  abuse");


    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

}
