package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class OtherConcerns {
    private final String c1AkeepingSafeStatement;
    private final String c1AsupervisionAgreementDetails;
    private final YesOrNo c1AagreementOtherWaysDetails;
    private final YesOrNo c1AotherConcernsDrugs;
    private final String c1AotherConcernsDrugsDetails;
    private final YesOrNo c1AchildSafetyConcerns;
    private final String c1AchildSafetyConcernsDetails;
}
