package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Otherconcerns {
    private final String c1A_keepingSafeStatement;
    private final String c1A_supervisionAgreementDetails;
    private final YesOrNo c1A_agreementOtherWaysDetails;
    private final YesOrNo c1A_otherConcernsDrugs;
    private final String c1A_otherConcernsDrugsDetails;
    private final YesOrNo c1A_childSafetyConcerns;
    private final String c1A_childSafetyConcernsDetails;
}
