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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GatekeepingDetails {
    @CCD(
            label = "Do you want to send this case to a specific gatekeeper?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("isSpecificGateKeeperNeeded")
    private final YesOrNo isSpecificGateKeeperNeeded;

    @CCD(label = "Judge or legal adviser?", searchable = false)
    @JsonProperty("isJudgeOrLegalAdviserGatekeeping")
    private final SendToGatekeeperTypeEnum isJudgeOrLegalAdviserGatekeeping;

    @CCD(label = "Name of the judge", searchable = false, typeOverride = FieldType.JudicialUser)
    @JsonProperty("judgeName")
    private final JudicialUser judgeName;

    @CCD(label = "Name of the legal adviser", searchable = false, typeOverride = FieldType.DynamicList)
    @JsonProperty("legalAdviserList")
    private final DynamicList legalAdviserList;

    @CCD(label = "judgePersonalCode", showCondition = "judgePersonalCode=\"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("judgePersonalCode")
    private final String judgePersonalCode;


}

