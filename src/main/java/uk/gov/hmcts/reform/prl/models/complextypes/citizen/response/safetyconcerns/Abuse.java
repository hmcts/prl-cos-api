package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Abuse {
    @CCD(label = " ", searchable = false)
    private final String behaviourDetails;
    @CCD(label = " ", searchable = false)
    private final String behaviourStartDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isOngoingBehaviour;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo seekHelpFromPersonOrAgency;
    @CCD(label = " ", searchable = false)
    private final String seekHelpDetails;
    @CCD(label = " ", searchable = false)
    private final String childrenConcernedAbout;
}

