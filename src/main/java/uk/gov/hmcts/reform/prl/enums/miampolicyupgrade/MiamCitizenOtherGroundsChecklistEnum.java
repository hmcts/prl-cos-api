package uk.gov.hmcts.reform.prl.enums.miampolicyupgrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamCitizenOtherGroundsChecklistEnum {

    @JsonProperty("applyingForWithoutNoticeHearing")
    applyingForWithoutNoticeHearing("applyingForWithoutNoticeHearing", "miamPolicyUpgradeOtherGrounds_Value_1"),
    @JsonProperty("under18")
    under18("under18","miamPolicyUpgradeOtherGrounds_Value_2"),
    @JsonProperty("canNotAccessMediator")
    canNotAccessMediator("canNotAccessMediator", "mpuCanNotAccessMediator"),
    @JsonProperty("noAppointmentAvailable")
    noAppointmentAvailable("noAppointmentAvailable","miamPolicyUpgradeOtherGrounds_Value_3"),
    @JsonProperty("disability")
    disability("disability", "miamPolicyUpgradeOtherGrounds_Value_4"),
    @JsonProperty("noMediatorIn15mile")
    noMediatorIn15mile("noMediatorIn15mile", "miamPolicyUpgradeOtherGrounds_Value_5"),
    @JsonProperty("inPrison")
    inPrison("inPrison","miamPolicyUpgradeOtherGrounds_Value_6"),
    @JsonProperty("bailThatPreventContact")
    bailThatPreventContact("bailThatPreventContact","miamPolicyUpgradeOtherGrounds_Value_6"),
    @JsonProperty("releaseFromPrisonOnLicence")
    releaseFromPrisonOnLicence("releaseFromPrisonOnLicence","miamPolicyUpgradeOtherGrounds_Value_6");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MiamCitizenOtherGroundsChecklistEnum getValue(String key) {
        return MiamCitizenOtherGroundsChecklistEnum.valueOf(key);
    }

}
