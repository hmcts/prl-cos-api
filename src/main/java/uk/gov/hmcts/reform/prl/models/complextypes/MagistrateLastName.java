package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class MagistrateLastName {
    @JsonProperty("magistrateLastName.lastName")
    private final String lastName;

    @JsonCreator
    public MagistrateLastName(String lastName) {
        this.lastName = lastName;
    }
}
