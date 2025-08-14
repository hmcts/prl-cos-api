package uk.gov.hmcts.reform.prl.enums.citizen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;

import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.childProtectionConcern;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.other;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.previousMIAMattendance;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.urgency;

@Getter
@RequiredArgsConstructor
public enum MiamExemptionMapperEnum {
    domesticViolence(MiamExemptionsChecklistEnum.domesticViolence),
    childProtection(childProtectionConcern),
    urgentHearing(urgency),
    previousMIAMOrExempt(previousMIAMattendance),
    validExemption(other);

    public MiamExemptionsChecklistEnum getValue() {
        return value;
    }

    private final MiamExemptionsChecklistEnum value;
}