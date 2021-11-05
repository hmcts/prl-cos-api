package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MIAMPreviousAttendanceChecklistEnum {

    MIAMPreviousAttendanceChecklistEnum_Value_1("In the 4 months prior to making the application, the person attended a MIAM or participated in another form of non-court dispute resolution relating to the same or substantially the same dispute"),
    MIAMPreviousAttendanceChecklistEnum_Value_2("At the time of making the application, the person is participating in another form of non-court dispute resolution relating to the same or substantially the same dispute"),
    MIAMPreviousAttendanceChecklistEnum_Value_3("In the 4 months prior to making the application, the person filed a relevant family application confirming that a MIAM exemption applied and that application related to the same or substantially the same dispute"),
    MIAMPreviousAttendanceChecklistEnum_Value_4("The application would be made in existing proceedings which are continuing and the prospective applicant attended a MIAM before initiating those proceedings"),
    MIAMPreviousAttendanceChecklistEnum_Value_5("The application would be made in existing proceedings which are continuing and a MIAM exemption applied to the application for those proceedings");

    private final String displayedValue;


}
