package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class PartyNameDA {
    @JsonProperty("partyName")
    private final String partyName;

    @JsonCreator
    public PartyNameDA(String partyName) {
        this.partyName  = partyName;
    }
}
