package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactInformation {
    @JsonProperty("addressLine1")
    private String addressLine1;

    @JsonProperty("addressLine2")
    private String addressLine2;

    @JsonProperty("addressLine3")
    private String addressLine3;

    @JsonProperty("country")
    private String country;

    @JsonProperty("county")
    private String county;

    @JsonProperty("dxAddress")
    private List<DxAddress> dxAddress;

    @JsonProperty("postCode")
    private String postCode;

    @JsonProperty("townCity")
    private String townCity;

    @JsonIgnore
    public Address toAddress() {
        return Address.builder()
            .addressLine1(getAddressLine1())
            .addressLine2(getAddressLine2())
            .addressLine3(getAddressLine3())
            .county(getCounty())
            .country(getCountry())
            .postCode(getPostCode())
            .postTown(getTownCity())
            .build();
    }
}
