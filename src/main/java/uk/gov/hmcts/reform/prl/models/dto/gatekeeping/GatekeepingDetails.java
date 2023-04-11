package uk.gov.hmcts.reform.prl.models.dto.gatekeeping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.SendToGatekeeperTypeEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GatekeepingDetails {
    @JsonProperty("isSpecificGateKeeperNeeded")
    private final YesOrNo isSpecificGateKeeperNeeded;

    @JsonProperty("isJudgeOrLegalAdviserGatekeeping")
    private final SendToGatekeeperTypeEnum isJudgeOrLegalAdviserGatekeeping;

    @JsonProperty("judgeName")
    private final JudicialUser judgeName;

    @JsonProperty("legalAdviserList")
    private final DynamicList legalAdviserList;

    @JsonProperty("judgePersonalCode")
    private final String judgePersonalCode;


}

