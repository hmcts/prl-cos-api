package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Organisation;

@Builder(toBuilder = true)
@Data
public class Barrister {
    private final String barristerFirstName;
    private final String barristerLastName;
    private final String barristerEmail;
    private final Organisation barristerOrg;
}
