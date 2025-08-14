package uk.gov.hmcts.reform.prl.enums.citizen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;

import static uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_10;
import static uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_11;
import static uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_4;
import static uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_5;
import static uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_6;
import static uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_7;
import static uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_8;

@Getter
@RequiredArgsConstructor
public enum MiamNotAttendingReasonsMapperEnum {
    noSufficientContactDetails(miamOtherGroundsChecklistEnum_Value_4),
    applyingForWithoutNoticeHearing(miamOtherGroundsChecklistEnum_Value_5),
    mediatorDoesNotHaveDisabilityAccess(miamOtherGroundsChecklistEnum_Value_6),
    noMediatorAppointment(miamOtherGroundsChecklistEnum_Value_10),
    noAuthorisedFamilyMediator(miamOtherGroundsChecklistEnum_Value_11),
    notAttendingAsInPrison(miamOtherGroundsChecklistEnum_Value_7),
    notHabituallyResident(miamOtherGroundsChecklistEnum_Value_8),
    //TODO
    under18(miamOtherGroundsChecklistEnum_Value_8);

    public MiamOtherGroundsChecklistEnum getValue() {
        return value;
    }

    private final MiamOtherGroundsChecklistEnum value;
}