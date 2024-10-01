package uk.gov.hmcts.reform.prl.models.dto.ccd.restrictedcaseaccessmanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.restrictedcaseaccessmanagement.CaseSecurityClassificationEnum;

@Data
@Builder
@AllArgsConstructor
public class CaseAccessStatusAndReason {
    private String markAsPrivateReason;
    private String markAsPublicReason;
    private String markAsRestrictedReason;

    private CaseSecurityClassificationEnum caseSecurityClassification;
}
