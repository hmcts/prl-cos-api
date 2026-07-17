package uk.gov.hmcts.reform.prl.models.dto.ccd.restrictedcaseaccessmanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.restrictedcaseaccessmanagement.CaseSecurityClassificationEnum;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess;

@Data
@Builder
@AllArgsConstructor
public class CaseAccessStatusAndReason {
    @CCD(
            label = "Briefly explain why this case should be private.",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class}
    )
    private String markAsPrivateReason;
    @CCD(
            label = "Briefly explain why this case should be public.",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class}
    )
    private String markAsPublicReason;
    @CCD(
            label = "Briefly explain why this case should be restricted.",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class}
    )
    private String markAsRestrictedReason;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CaseSecurityClassificationEnum",
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class}
    )
    private CaseSecurityClassificationEnum caseSecurityClassification;
}
