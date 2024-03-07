package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;

@Builder
@Data
public class AllocatedJudge {
    private final String judgePersonalCode;
    private final String tierOfJudiciaryType;
    private final String lastName;
    private final String emailAddress;
    private final String courtName;
    private final YesOrNo isSpecificJudgeOrLegalAdviserNeeded;
    private final AllocatedJudgeTypeEnum isJudgeOrLegalAdviser;
    private final String tierOfJudge;
}
