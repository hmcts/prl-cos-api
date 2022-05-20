package uk.gov.hmcts.reform.prl.enums.ServiceOfApplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ChildArrangementsSpecificProhibitedOrderEnum {

    childArrangementsSpecificProhibitedOrder("Child arrangements, specific issue or prohibited steps order (C43)");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ChildArrangementsSpecificProhibitedOrderEnum getValue(String key) {
        return ChildArrangementsSpecificProhibitedOrderEnum.valueOf(key);
    }

}
