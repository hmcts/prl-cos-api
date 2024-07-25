package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class C100Address  {
    @JsonProperty("AddressLine1")
    private  String addressLine1;
    @JsonProperty("AddressLine2")
    private  String addressLine2;
    @JsonProperty("AddressLine3")
    private  String addressLine3;
    @JsonProperty("PostTown")
    private  String postTown;
    @JsonProperty("County")
    private  String country;
    @JsonProperty("PostCode")
    private  String postCode;
    @JsonProperty("addressHistory")
    private String addressHistory;
    @JsonProperty("provideDetailsOfPreviousAddresses")
    private String provideDetailsOfPreviousAddresses;
}
