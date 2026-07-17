package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavRAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessUrgentHelpWithFees {

    @CCD(
            label = "Select the help with fees application to be reviewed",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class}
    )
    private DynamicList hwfAppList;
    @CCD(
            label = "Add a case note",
            hint = "Keep your note brief and write in short sentences",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class}
    )
    private String addHwfCaseNoteShort;
    @CCD(
            label = "Is there an outstanding balance to be paid?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class}
    )
    private YesOrNo outstandingBalance;
    @CCD(
            label = "Has a manager agreed to process this application before the payment has been made?",
            hint = "The manager may be a delivery manager or a senior manager",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class}
    )
    private YesOrNo managerAgreedApplicationBeforePayment;
    @CCD(
            label = " ",
            hint = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawLaCruAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class, CourtnavRAccess.class}
    )
    private String isTheCaseInDraftState;
}
