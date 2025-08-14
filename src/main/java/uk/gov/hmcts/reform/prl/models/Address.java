package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Address {

    //TODO: need to ensure this can handle null values - see the civil implementation

    @JsonProperty("AddressLine1")
    private final String addressLine1;
    @JsonProperty("AddressLine2")
    private final String addressLine2;
    @JsonProperty("AddressLine3")
    private final String addressLine3;
    @JsonProperty("PostTown")
    private final String postTown;
    @JsonProperty("County")
    private final String county;
    @JsonProperty("Country")
    private final String country;
    @JsonProperty("PostCode")
    private final String postCode;


}
