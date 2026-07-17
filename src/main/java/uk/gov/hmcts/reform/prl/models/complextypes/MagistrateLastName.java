package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder
public class MagistrateLastName {
    @CCD(label = "Magistrate's full name", searchable = false)
    @JsonProperty("lastName")
    private final String lastName;

    @JsonCreator
    public MagistrateLastName(String lastName) {
        this.lastName = lastName;
    }
}
