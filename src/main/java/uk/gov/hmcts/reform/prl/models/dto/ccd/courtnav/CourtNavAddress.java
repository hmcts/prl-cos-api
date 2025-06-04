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
public class CourtNavAddress {

    @JsonProperty("AddressLine1")
    private final String addressLine1;

    @JsonProperty("AddressLine2")
    private final String addressLine2;

    @JsonProperty("AddressLine3")
    private final String addressLine3;

    @JsonProperty("PostTown")
    private final String postTown;

    @JsonProperty("PostCode")
    @Size(max = 14)
    private final String postCode;

    @JsonProperty("County")
    private final String county;

    @JsonProperty("Country")
    private final String country;
}
