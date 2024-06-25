package uk.gov.hmcts.reform.prl.enums.miampolicyupgrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamCitizenPreviousAttendanceReasonEnum {

    @JsonProperty("fourMonthsPriorAttended")
    fourMonthsPriorAttended("fourMonthsPriorAttended","miamPolicyUpgradePreviousAttendance_Value_1"),
    @JsonProperty("miamPolicyUpgradePreviousAttendance_Value_2")
    miamExamptionApplied("miamExamptionApplied","miamPolicyUpgradePreviousAttendance_Value_2");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MiamCitizenPreviousAttendanceReasonEnum getValue(String key) {
        return MiamCitizenPreviousAttendanceReasonEnum.valueOf(key);
    }

}
