package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;

@Data
@Builder
public class OtherPersonWhoLivesWithChildDetails {

    private String firstName;
    private String lastName;
    private String relationshipToChildDetails;
    private Address address;
    private YesOrNo isPersonIdentityConfidential;
}
