package uk.gov.hmcts.reform.prl.enums.miampolicyupgrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamCitizenUrgencyReasonEnum {

    @JsonProperty("freedomPhysicalSafety")
    freedomPhysicalSafety("freedomPhysicalSafety", "miamPolicyUpgradeUrgencyReason_Value_1"),
    @JsonProperty("freedomPhysicalSafetyInFamily")
    freedomPhysicalSafetyInFamily("freedomPhysicalSafetyInFamily", "miamPolicyUpgradeUrgencyReason_Value_1"),
    @JsonProperty("riskSafetyInHome")
    riskSafetyInHome("riskSafetyInHome", "miamPolicyUpgradeUrgencyReason_Value_1"),
    @JsonProperty("riskOfHarmToChildren")
    riskOfHarmToChildren("riskOfHarmToChildren", "miamPolicyUpgradeUrgencyReason_Value_2"),

    @JsonProperty("unlawfullyRemovedFromUK")
    unlawfullyRemovedFromUK(
        "unlawfullyRemovedFromUK", "miamPolicyUpgradeUrgencyReason_Value_3"),
    @JsonProperty("riskOfUnfairCourtDecision")
    riskOfUnfairCourtDecision("riskOfUnfairCourtDecision", "miamPolicyUpgradeUrgencyReason_Value_4"),
    @JsonProperty("riskUnreasonableFinancialHardship")
    riskUnreasonableFinancialHardship("riskUnreasonableFinancialHardship", "miamPolicyUpgradeUrgencyReason_Value_5"),
    @JsonProperty("riskOfIrretrievableProblems")
    riskOfIrretrievableProblems("riskOfIrretrievableProblems", "miamPolicyUpgradeUrgencyReason_Value_6"),
    @JsonProperty("riskOfCourtProceedingsDispute")
    riskOfCourtProceedingsDispute("riskOfCourtProceedingsDispute", "miamPolicyUpgradeUrgencyReason_Value_7");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MiamCitizenUrgencyReasonEnum getValue(String key) {
        return MiamCitizenUrgencyReasonEnum.valueOf(key);
    }

}
