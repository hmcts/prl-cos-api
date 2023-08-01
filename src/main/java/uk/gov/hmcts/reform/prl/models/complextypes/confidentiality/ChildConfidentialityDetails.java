package uk.gov.hmcts.reform.prl.models.complextypes.confidentiality;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.Collections;
import java.util.List;

@Builder
@Data
public class ChildConfidentialityDetails {
    private final String firstName;
    private final String lastName;
    private List<Element<OtherPersonConfidentialityDetails>> otherPerson = Collections.emptyList();
}
