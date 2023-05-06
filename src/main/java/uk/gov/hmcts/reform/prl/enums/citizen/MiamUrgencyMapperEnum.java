package uk.gov.hmcts.reform.prl.enums.citizen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;

import static uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_2;
import static uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_3;
import static uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_4;
import static uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_5;
import static uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_6;
import static uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_7;

@Getter
@RequiredArgsConstructor
public enum MiamUrgencyMapperEnum {
    freedomPhysicalSafety(miamUrgencyReasonChecklistEnum_Value_1),
    freedomPhysicalSafetyInFamily(miamUrgencyReasonChecklistEnum_Value_1),
    riskSafetyInHome(miamUrgencyReasonChecklistEnum_Value_1),
    riskUnreasonableFinancialHardship(miamUrgencyReasonChecklistEnum_Value_3),
    riskOfHarmToChildren(miamUrgencyReasonChecklistEnum_Value_7),
    unlawfullyRemovedFromUK(miamUrgencyReasonChecklistEnum_Value_6),
    riskOfUnfairCourtDecision(miamUrgencyReasonChecklistEnum_Value_2),
    riskOfIrretrievableProblems(miamUrgencyReasonChecklistEnum_Value_4),
    riskOfCourtProceedingsDispute(miamUrgencyReasonChecklistEnum_Value_5);

    public MiamUrgencyReasonChecklistEnum getValue() {
        return value;
    }

    private final MiamUrgencyReasonChecklistEnum value;
}