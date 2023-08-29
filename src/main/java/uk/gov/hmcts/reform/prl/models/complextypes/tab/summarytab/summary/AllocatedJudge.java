package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllocatedJudge {
    private final String judgePersonalCode;
    private final String tierOfJudiciaryType;
    private final String lastName;
    private final String emailAddress;
    private final String courtName;
    private final YesOrNo isSpecificJudgeOrLegalAdviserNeeded;
    private final AllocatedJudgeTypeEnum isJudgeOrLegalAdviser;
}
