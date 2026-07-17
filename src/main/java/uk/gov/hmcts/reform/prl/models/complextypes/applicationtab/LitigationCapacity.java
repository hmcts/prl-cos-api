package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class LitigationCapacity {

    @CCD(
            label = "*Give details of any factors affecting litigation capacity",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String litigationCapacityFactors;
    @CCD(
            label = "Provide details of any referral to or assessment by the Adult Learning Disability team, and/or any adult health service, where known, together with the outcome",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String litigationCapacityReferrals;
    @CCD(
            label = "Are you aware of any other factors which may affect the ability of the person concerned to take part in the proceedings?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo litigationCapacityOtherFactors;
    @CCD(label = "*Give details", searchable = false, typeOverride = FieldType.TextArea)
    private final String litigationCapacityOtherFactorsDetails;

}
