package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Builder
@Data
public class Miam {
    private final YesOrNo applicantAttendedMiam;
    private final YesOrNo claimingExemptionMiam;
    private final YesOrNo familyMediatorMiam;
    private final String mediatorRegistrationNumber;
    private final String familyMediatorServiceName;
    private final String soleTraderName;
}
