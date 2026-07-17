package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
public class PartyNameDA {
    @CCD(label = "Names of parties raising domestic abuse issues", searchable = false)
    @JsonProperty("partyName")
    private final String partyName;

    @JsonCreator
    public PartyNameDA(String partyName) {
        this.partyName  = partyName;
    }
}
