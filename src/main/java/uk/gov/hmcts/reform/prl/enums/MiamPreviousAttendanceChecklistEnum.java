package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MiamPreviousAttendanceChecklistEnum {

    @JsonProperty("miamPreviousAttendanceChecklistEnum_Value_1")
    MIAMPreviousAttendanceChecklistEnum_Value_1(
        "In the 4 months prior to making the application, the person attended a MIAM or participated in another form "
            +
            "of non-court dispute resolution relating to the same or substantially the same dispute"),
    @JsonProperty("miamPreviousAttendanceChecklistEnum_Value_2")
    MIAMPreviousAttendanceChecklistEnum_Value_2(
        "At the time of making the application, the person is participating in another form of non-court dispute "
            +
            "resolution relating to the same or substantially the same dispute"),
    @JsonProperty("miamPreviousAttendanceChecklistEnum_Value_3")
    MIAMPreviousAttendanceChecklistEnum_Value_3(
        "In the 4 months prior to making the application, the person filed a relevant family application confirming "
            +
            "that a MIAM exemption applied and that application related to the same or substantially the same dispute"),
    @JsonProperty("miamPreviousAttendanceChecklistEnum_Value_4")
    MIAMPreviousAttendanceChecklistEnum_Value_4(
        "The application would be made in existing proceedings which are continuing and the prospective applicant "
            +
            "attended a MIAM before initiating those proceedings"),
    @JsonProperty("miamPreviousAttendanceChecklistEnum_Value_5")
    MIAMPreviousAttendanceChecklistEnum_Value_5(
        "The application would be made in existing proceedings which are continuing and a MIAM exemption applied to "
            +
            "the application for those proceedings");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static OrderTypeEnum getValue(String key) {
        return OrderTypeEnum.valueOf(key);
    }


}
