package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String country;
    private String county;
    private List<DxAddress> dxAddress;
    private String postCode;
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
