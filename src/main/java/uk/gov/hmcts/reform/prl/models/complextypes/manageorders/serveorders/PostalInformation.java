package uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Address;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class PostalInformation {

    private final String postalName;
    private final Address postalAddress;
}
