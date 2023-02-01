package uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Address;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class PostalInformation {

    @JsonProperty("postalName")
    private final String postalName;

    @JsonProperty("postalAddress")
    private final Address postalAddress;
}
