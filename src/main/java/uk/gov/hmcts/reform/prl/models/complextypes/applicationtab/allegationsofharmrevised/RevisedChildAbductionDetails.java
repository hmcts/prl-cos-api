package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;


@Data
@Builder
public class RevisedChildAbductionDetails {

    private final String newChildAbductionReasons;
    private final YesOrNo newPreviousAbductionThreats;
    private final String newPreviousAbductionThreatsDetails;
    private final String newChildrenLocationNow;
    private final YesOrNo newAbductionPassportOfficeNotified;
    private final YesOrNo newAbductionPreviousPoliceInvolvement;
    private final String newAbductionPreviousPoliceInvolvementDetails;
    private final YesOrNo newAbductionChildHasPassport;
    private YesOrNo newChildHasMultiplePassports;
    private String newChildPassportPossession;
}
