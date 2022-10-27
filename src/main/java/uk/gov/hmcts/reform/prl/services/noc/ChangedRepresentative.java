package uk.gov.hmcts.reform.prl.services.noc;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.prl.models.Organisation;

@Value
@Builder
public class ChangedRepresentative {
    String firstName;
    String lastName;
    String email;
    Organisation organisation;
}
