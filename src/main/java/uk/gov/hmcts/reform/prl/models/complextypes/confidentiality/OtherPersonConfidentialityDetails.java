package uk.gov.hmcts.reform.prl.models.complextypes.confidentiality;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;

@Builder
@Data
public class OtherPersonConfidentialityDetails {
    private final String firstName;
    private final String lastName;
    private final String relationshipToChildDetails;
    private final Address address;
    private final String email;
    private final String phoneNumber;
    private final YesOrNo isPersonIdentityConfidential;
}
