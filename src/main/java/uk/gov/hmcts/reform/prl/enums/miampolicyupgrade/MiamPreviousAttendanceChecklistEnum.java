package uk.gov.hmcts.reform.prl.enums.miampolicyupgrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamPreviousAttendanceChecklistEnum {

    @JsonProperty("miamPolicyUpgradePreviousAttendance_Value_1")
    miamPolicyUpgradePreviousAttendance_Value_1(
        "miamPolicyUpgradePreviousAttendance_Value_1",
        "In the 4 months prior to making the application, the person attended a MIAM"
            + " or a non-court dispute resolution process relating to the same or substantially the same dispute; and where the applicant attended"
            + " a non-court dispute resolution process, there is evidence of that attendance in the form of written confirmation from"
            + " the dispute resolution provider."
    ),
    @JsonProperty("miamPolicyUpgradePreviousAttendance_Value_2")
    miamPolicyUpgradePreviousAttendance_Value_2(
        "miamPolicyUpgradePreviousAttendance_Value_2",
        "The application would be made in existing proceedings which are continuing"
            + " and the prospective applicant attended a MIAM before initiating those proceedings."
            + " You will need to upload the mediator’s certificate. If you are the respondent in"
            + " existing proceedings, provide the date of the MIAM alongside the name and contact"
            + " details of the MIAM provider in the text area."
    );

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MiamPreviousAttendanceChecklistEnum getValue(String key) {
        return MiamPreviousAttendanceChecklistEnum.valueOf(key);
    }

}
