package uk.gov.hmcts.reform.prl.models.complextypes.confidentiality;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Builder
@Data
public class ChildConfidentialityDetails {
    private final String firstName;
    private final String lastName;
    private final List<Element<OtherPersonConfidentialityDetails>> otherPerson;
}
