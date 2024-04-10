package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MiamPolicyUpgradeExemptions {

    private final String mpuReasonsForMiamExemption;
    private final String mpuDomesticAbuseEvidence;
    private final String mpuChildProtectionEvidence;
    private final String mpuUrgencyEvidence;
    private final String mpuPreviousAttendenceEvidence;
    private final String mpuOtherGroundsEvidence;

}
