package uk.gov.hmcts.reform.prl.models.complextypes.confidentiality;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Builder
@Data
public class ChildConfidentialityDetails {
    @CCD(label = "*First Name", searchable = false)
    private final String firstName;
    @CCD(label = "*Last Name", searchable = false)
    private final String lastName;
    @CCD(label = "Another person", searchable = false)
    private final List<Element<OtherPersonConfidentialityDetails>> otherPerson;
}
