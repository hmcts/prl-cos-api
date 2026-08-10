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
public enum AmendOrderCheckEnum {
    @CCD(label = "A judge or legal adviser needs to check the order")
    @JsonProperty("judgeOrLegalAdvisorCheck")
    judgeOrLegalAdvisorCheck("judgeOrLegalAdvisorCheck", "A judge or legal adviser needs to check the order"),

    @CCD(label = "A manager needs to check the order")
    @JsonProperty("managerCheck")
    managerCheck("managerCheck", "A manager needs to check the order"),

    @CCD(label = "No checks are required")
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
