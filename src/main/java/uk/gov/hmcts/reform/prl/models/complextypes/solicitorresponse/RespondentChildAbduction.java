package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.solicitorresponse.WhomConsistPassportList;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class RespondentChildAbduction {

    private final String reasonForChildAbductionBelief;
    private final YesOrNo previousThreatsForChildAbduction;
    private final String previousThreatsForChildAbductionDetails;
    private final String whereIsChild;
    private final YesOrNo hasPassportOfficeNotified;
    private final YesOrNo anyOrgInvolvedInPreviousAbduction;
    private final String anyOrgInvolvedInPreviousAbductionDetails;
    private final YesOrNo childrenHavePassport;
    private final YesOrNo childrenHaveMoreThanOnePassport;
    private final WhomConsistPassportList whoHasChildPassport;
    private final String whoHasChildPassportOther;

}
