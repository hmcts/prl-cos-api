package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
public class LitigationCapacity {

    private final String litigationCapacityFactors;
    private final String litigationCapacityReferrals;
    private final YesOrNo litigationCapacityOtherFactors;
    private final String litigationCapacityOtherFactorsDetails;

}
