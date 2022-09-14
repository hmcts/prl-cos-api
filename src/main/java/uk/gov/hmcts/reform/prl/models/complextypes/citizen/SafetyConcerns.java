package uk.gov.hmcts.reform.prl.models.complextypes.citizen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class SafetyConcerns {

    private final YesOrNo domesticAbuseDetails;
    private final YesOrNo isSexuallyAbused;
    private final AbuseDetails sexualAbuseDescription;
    private final YesOrNo isPhysicallyAbused;
    private final AbuseDetails physicalAbuseDescription;
    private final YesOrNo isFinanciallyAbused;
    private final AbuseDetails financialAbuseDescription;
    private final YesOrNo isPsychologicallyAbused;
    private final AbuseDetails psychologicalAbuseDescription;
    private final YesOrNo isEmotinallyAbused;
    private final AbuseDetails emotionalAbuseDescription;
}
