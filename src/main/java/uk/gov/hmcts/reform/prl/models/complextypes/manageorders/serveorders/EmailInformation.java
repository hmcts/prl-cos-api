package uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class EmailInformation {

    @JsonProperty("emailName")
    private final String emailName;

    @JsonProperty("emailAddress")
    private final String emailAddress;
}
