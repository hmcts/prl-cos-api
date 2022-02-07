package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
public class ChildAbductionDetails {

    private final String childAtRiskOfAbductionReason;
    private final YesOrNo previousAbductionThreats;
    private final String previousAbductionThreatsDetails;
    private final String childrenLocationNow;
    private final YesOrNo abductionPassportOfficeNotified;
    private final YesOrNo abductionPreviousPoliceInvolvement;
    private final String abductionPreviousPoliceInvolvementDetails;

}
