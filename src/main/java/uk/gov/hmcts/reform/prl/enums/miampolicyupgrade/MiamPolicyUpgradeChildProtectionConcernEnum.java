package uk.gov.hmcts.reform.prl.enums.miampolicyupgrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamPolicyUpgradeChildProtectionConcernEnum {

    @JsonProperty("mpuChildProtectionConcern_value_1")
    mpuChildProtectionConcern_value_1(
        "mpuChildProtectionConcern_value_1",
        "The subject of enquiries by a local authority under section 47 of the Children Act 1989 Act"
    ),
    @JsonProperty("mpuChildProtectionConcern_value_2")
    mpuChildProtectionConcern_value_2(
        "MIAMChildProtectionConcernChecklistEnum_value_2",
        "The subject of a child protection plan put in place by a local authority"
    );

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MiamPolicyUpgradeChildProtectionConcernEnum getValue(String key) {
        return MiamPolicyUpgradeChildProtectionConcernEnum.valueOf(key);
    }

}
