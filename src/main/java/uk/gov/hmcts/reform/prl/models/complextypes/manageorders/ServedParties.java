package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ServedParties {

    @JsonProperty("partyId")
    private final String partyId;

    @JsonProperty("partyName")
    private final String partyName;
}
