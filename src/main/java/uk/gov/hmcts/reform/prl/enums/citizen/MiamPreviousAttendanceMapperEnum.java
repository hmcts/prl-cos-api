package uk.gov.hmcts.reform.prl.enums.citizen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;

import static uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_2;
import static uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_3;
import static uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_4;
import static uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_5;

@Getter
@RequiredArgsConstructor
public enum MiamPreviousAttendanceMapperEnum {
    fourMonthsPriorAttended(miamPreviousAttendanceChecklistEnum_Value_1),
    onTimeParticipation(miamPreviousAttendanceChecklistEnum_Value_2),
    beforeInitiationProceeding(miamPreviousAttendanceChecklistEnum_Value_4),
    fourMonthsPriorFiled(miamPreviousAttendanceChecklistEnum_Value_3),
    miamExamptionApplied(miamPreviousAttendanceChecklistEnum_Value_5),
    beforStatingApplication(miamPreviousAttendanceChecklistEnum_Value_4);

    public MiamPreviousAttendanceChecklistEnum getValue() {
        return value;
    }

    private final uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum value;
}