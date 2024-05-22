package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@Jacksonized
public class CourtnavAddress {

    @JsonProperty("addressLine1")
    private final String addressLine1;
    @JsonProperty("addressLine2")
    private final String addressLine2;
    @JsonProperty("addressLine3")
    private final String addressLine3;
    @JsonProperty("postTown")
    private final String postTown;
    @JsonProperty("postCode")
    @Size(max = 14)
    private final String postCode;
    @JsonProperty("county")
    private final String county;
    @JsonProperty("country")
    private final String country;

}
