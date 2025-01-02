package uk.gov.hmcts.reform.prl.enums.miampolicyupgrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;


@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamCitizenExemptionsReasonEnum {

    @JsonProperty("domesticViolence")
    domesticViolence("domesticViolence", "mpuDomesticAbuse"),
    @JsonProperty("childProtection")
    childProtection("childProtection", "mpuChildProtectionConcern"),
    @JsonProperty("urgentHearing")
    urgentHearing("urgentHearing", "mpuUrgency"),
    @JsonProperty("previousMIAMOrExempt")
    previousMIAMOrExempt("previousMIAMOrExempt", "mpuPreviousMiamAttendance"),
    @JsonProperty("validExemption")
    validExemption("validExemption", "mpuOther");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MiamCitizenExemptionsReasonEnum getValue(String key) {
        return MiamCitizenExemptionsReasonEnum.valueOf(key);
    }

}
