package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum AmendOrderCheckEnum {
    @JsonProperty("judgeOrLegalAdvisorCheck")
    judgeOrLegalAdvisorCheck("judgeOrLegalAdvisorCheck", "A judge or legal adviser needs to check the order"),

    @JsonProperty("managerCheck")
    managerCheck("managerCheck", "A manager needs to check the order"),

    @JsonProperty("noCheck")
    noCheck("noCheck", "No checks are required");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static AmendOrderCheckEnum getValue(String key) {
        return AmendOrderCheckEnum.valueOf(key);
    }
}
