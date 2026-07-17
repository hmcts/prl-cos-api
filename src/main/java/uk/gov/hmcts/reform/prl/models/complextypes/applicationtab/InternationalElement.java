package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class InternationalElement {
    @CCD(
            label = "Do you have any reason to believe that any child, parent or potentially significant adult in the child’s life may be habitually resident in another country abroad or in Scotland or Northern Ireland?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo habitualResidentInOtherState;
    @CCD(label = "Give reason", searchable = false, typeOverride = FieldType.TextArea)
    private final String habitualResidentInOtherStateGiveReason;
    @CCD(
            label = "Do you have any reason to believe that there may be an issue as to jurisdiction, relating to a country abroad or to Scotland or Northern Ireland, in this case?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo jurisdictionIssue;
    @CCD(label = "Give reason", searchable = false, typeOverride = FieldType.TextArea)
    private final String jurisdictionIssueGiveReason;
    @CCD(
            label = "Has a request been made or should a request be made to a Central Authority or other competent authority in a foreign state or a consular authority in England and Wales?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo requestToForeignAuthority;
    @CCD(label = "Give reason", searchable = false, typeOverride = FieldType.TextArea)
    private final String requestToForeignAuthorityGiveReason;
}
