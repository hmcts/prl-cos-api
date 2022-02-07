package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;

@Data
@Builder
public class OtherPersonWhoLivesWithChild {

    private final String firstName;
    private final String lastName;
    private final String relationshipToChildDetails;
    private final Address address;
    private final YesOrNo isPersonIdentityConfidential;
}
