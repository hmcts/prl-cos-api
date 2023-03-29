package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AllocatedJudge {
    private final String judgeTitle;
    private final String lastName;
    private final String emailAddress;
    private final String courtName;

}
