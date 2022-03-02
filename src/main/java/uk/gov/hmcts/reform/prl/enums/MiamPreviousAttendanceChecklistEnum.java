package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamPreviousAttendanceChecklistEnum {

    @JsonProperty("miamPreviousAttendanceChecklistEnum_Value_1")
    miamPreviousAttendanceChecklistEnum_Value_1(
        "In the 4 months prior to making the application, the person attended a MIAM or participated in another form "
            +
            "of non-court dispute resolution relating to the same or substantially the same dispute"),
    @JsonProperty("miamPreviousAttendanceChecklistEnum_Value_2")
    miamPreviousAttendanceChecklistEnum_Value_2(
        "At the time of making the application, the person is participating in another form of non-court dispute "
            +
            "resolution relating to the same or substantially the same dispute"),
    @JsonProperty("miamPreviousAttendanceChecklistEnum_Value_3")
    miamPreviousAttendanceChecklistEnum_Value_3(
        "In the 4 months prior to making the application, the person filed a relevant family application confirming "
            +
            "that a MIAM exemption applied and that application related to the same or substantially the same dispute"),
    @JsonProperty("miamPreviousAttendanceChecklistEnum_Value_4")
    miamPreviousAttendanceChecklistEnum_Value_4(
        "The application would be made in existing proceedings which are continuing and the prospective applicant "
            +
            "attended a MIAM before initiating those proceedings"),
    @JsonProperty("miamPreviousAttendanceChecklistEnum_Value_5")
    miamPreviousAttendanceChecklistEnum_Value_5(
        "The application would be made in existing proceedings which are continuing and a MIAM exemption applied to "
            +
            "the application for those proceedings");

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
