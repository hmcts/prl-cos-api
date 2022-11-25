package uk.gov.hmcts.reform.prl.enums.citizen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;

import static uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum.MIAMChildProtectionConcernChecklistEnum_value_1;
import static uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum.MIAMChildProtectionConcernChecklistEnum_value_2;

@Getter
@RequiredArgsConstructor
public enum ChildProtectionMapperEnum {
    localAuthority(MIAMChildProtectionConcernChecklistEnum_value_1),
    childProtectionPlan(MIAMChildProtectionConcernChecklistEnum_value_2);

    public MiamChildProtectionConcernChecklistEnum getValue() {
        return value;
    }

    private final uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum value;
}
