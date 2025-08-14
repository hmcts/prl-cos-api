package uk.gov.hmcts.reform.prl.models.dto.gatekeeping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllocatedJudge {
    @JsonProperty("isSpecificJudgeOrLegalAdviserNeeded")
    private final YesOrNo isSpecificJudgeOrLegalAdviserNeeded;

    @JsonProperty("isJudgeOrLegalAdviser")
    private final AllocatedJudgeTypeEnum isJudgeOrLegalAdviser;

    @JsonProperty("judgeName")
    private final String judgeName;

    @JsonProperty("judgeEmail")
    private final String judgeEmail;

    @JsonProperty("legalAdviserList")
    private final DynamicList legalAdviserList;

    @JsonProperty("tierOfJudiciary")
    private final TierOfJudiciaryEnum tierOfJudiciary;

    @JsonProperty("judgePersonalCode")
    private final String judgePersonalCode;

    @JsonProperty("tierOfJudge")
    private final String tierOfJudge;
}
