package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MagistrateLastName {
    @JsonProperty("lastName")
    private final String lastName;

    @JsonCreator
    public MagistrateLastName(String lastName) {
        this.lastName = lastName;
    }
}
