package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class SolicitorInternationalElement {

    private final YesOrNo reasonForChild;
    private final String reasonForChildDetails;
    private final YesOrNo reasonForParent;
    private final String reasonForParentDetails;
    private final YesOrNo reasonForJurisdiction;
    private final String reasonForJurisdictionDetails;
    private final YesOrNo requestToAuthority;
    private final String requestToAuthorityDetails;

}
