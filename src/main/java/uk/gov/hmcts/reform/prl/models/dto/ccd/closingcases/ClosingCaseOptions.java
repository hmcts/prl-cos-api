package uk.gov.hmcts.reform.prl.models.dto.ccd.closingcases;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.closingcase.CaseClosingReasonForChildren;
import uk.gov.hmcts.reform.prl.models.complextypes.closingcase.DateFinalDecisionWasMade;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyCourtnavRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus3RolesBbdbyrAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClosingCaseOptions {

    @CCD(
            label = "Do all children in the case have final decisions?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawExternaluserViewonlyCourtnavRAccess.class}
    )
    @JsonProperty("isTheDecisionAboutAllChildren")
    private final YesOrNo isTheDecisionAboutAllChildren;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesBbdbyrAccess.class}
    )
    @JsonProperty("childOptionsForFinalDecision")
    private final DynamicMultiSelectList childOptionsForFinalDecision;
    @CCD(
            label = "Child",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesBbdbyrAccess.class}
    )
    @JsonProperty("finalOutcomeForChildren")
    private final List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesBbdbyrAccess.class}
    )
    @JsonProperty("dateFinalDecisionWasMade")
    private DateFinalDecisionWasMade dateFinalDecisionWasMade;
}
