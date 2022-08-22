package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    @JsonProperty("AddressLine1")
    private String mAddressLine1;
    @JsonProperty("Country")
    private String mCountry;
    @JsonProperty("County")
    private Object mCounty;
    @JsonProperty("PostCode")
    private String mPostCode;
    @JsonProperty("PostTown")
    private String mPostTown;
}
