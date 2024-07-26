package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Builder
@Data
public class MiamPolicyUpgrade {
    private final YesOrNo mpuChildInvolvedInMiam;
    private final YesOrNo mpuApplicantAttendedMiam;
    private final YesOrNo mpuClaimingExemptionMiam;
    private final String mediatorRegistrationNumber;
    private final String familyMediatorServiceName;
    private final String soleTraderName;
}
