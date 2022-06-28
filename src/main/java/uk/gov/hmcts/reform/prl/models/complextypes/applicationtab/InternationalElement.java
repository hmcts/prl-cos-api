package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Builder
@Data
public class InternationalElement {

    private final YesOrNo habitualResidentInOtherState;
    private final String habitualResidentInOtherStateGiveReason;
    private final YesOrNo jurisdictionIssue;
    private final String jurisdictionIssueGiveReason;
    private final YesOrNo requestToForeignAuthority;
    private final String requestToForeignAuthorityGiveReason;

}
