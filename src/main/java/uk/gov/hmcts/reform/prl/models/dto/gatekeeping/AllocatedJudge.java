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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllocatedJudge {
    @CCD(
            label = "is SpecificJudge Or LegalAdviserNeeded",
            showCondition = "isSpecificJudgeOrLegalAdviserNeeded=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("isSpecificJudgeOrLegalAdviserNeeded")
    private final YesOrNo isSpecificJudgeOrLegalAdviserNeeded;

    @CCD(label = "is Judge Or LegalAdviser", showCondition = "isJudgeOrLegalAdviser=\"DO_NOT_SHOW\"")
    @JsonProperty("isJudgeOrLegalAdviser")
    private final AllocatedJudgeTypeEnum isJudgeOrLegalAdviser;

    @CCD(ignore = true)
    @JsonProperty("judgeName")
    private final String judgeName;

    @CCD(ignore = true)
    @JsonProperty("judgeEmail")
    private final String judgeEmail;

    @CCD(label = "Name of legal adviser", searchable = false, typeOverride = FieldType.DynamicList)
    @JsonProperty("legalAdviserList")
    private final DynamicList legalAdviserList;

    @CCD(label = "Select the tier of judiciary", searchable = false)
    @JsonProperty("tierOfJudiciary")
    private final TierOfJudiciaryEnum tierOfJudiciary;

    @CCD(label = "judgePersonalCode", showCondition = "judgePersonalCode=\"DO_NOT_SHOW\"")
    @JsonProperty("judgePersonalCode")
    private final String judgePersonalCode;

    @CCD(label = "Tier of judge")
    @JsonProperty("tierOfJudge")
    private final String tierOfJudge;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Name of judge", searchable = false, typeOverride = FieldType.JudicialUser)
  private String judgeNameAndEmail;
  @CCD(label = "Tier of judiciary")
  private String tierOfJudiciaryType;
  @CCD(label = "Last name", searchable = false)
  private String lastName;
  @CCD(label = "Email address", searchable = false)
  private String emailAddress;
  @CCD(label = "Court name", searchable = false)
  private String courtName;
  // ==== end synthesised definition-only fields ====
}
