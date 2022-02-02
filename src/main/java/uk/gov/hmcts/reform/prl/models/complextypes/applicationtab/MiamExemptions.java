package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MiamExemptions {

    private final String reasonsForMiamExemption;
    private final String domesticViolenceEvidence;
    private final String urgencyEvidence;
    private final String childProtectionEvidence;
    private final String previousAttendenceEvidence;
    private final String otherGroundsEvidence;

}
