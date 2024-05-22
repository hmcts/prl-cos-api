package uk.gov.hmcts.reform.prl.enums.miampolicyupgrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamCitizenChildProtectionConcernEnum {

    @JsonProperty("childProtectionPlan")
    childProtectionPlan(
        "childProtectionPlan","mpuChildProtectionConcern_value_1"),
    @JsonProperty("localAuthority")
    localAuthority(
        "localAuthority", "MIAMChildProtectionConcernChecklistEnum_value_2");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MiamCitizenChildProtectionConcernEnum getValue(String key) {
        return MiamCitizenChildProtectionConcernEnum.valueOf(key);
    }

}
