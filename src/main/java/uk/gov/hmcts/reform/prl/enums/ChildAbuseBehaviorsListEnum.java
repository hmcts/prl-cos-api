package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ChildAbuseBehaviorsListEnum {

    @JsonProperty("ChildAbuseBehaviorsListEnum_value_1")
    ChildAbuseBehaviorsListEnum_value_1("Child1"),

    @JsonProperty("ChildAbuseBehaviorsListEnum_value_2")
    ChildAbuseBehaviorsListEnum_value_2("Child2"),

    @JsonProperty("ChildAbuseBehaviorsListEnum_value_3")
    ChildAbuseBehaviorsListEnum_value_3("All the children in this application");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

}
