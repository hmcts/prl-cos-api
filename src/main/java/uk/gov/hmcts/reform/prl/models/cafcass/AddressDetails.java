package uk.gov.hmcts.reform.prl.models.cafcass;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDetails {
    @JsonProperty("ADDRESS")
    private String address;

    @JsonProperty("SUB_BUILDING_NAME")
    private String buildingNumber;

    @JsonProperty("THOROUGHFARE_NAME")
    private String thoroughfareName;

    @JsonProperty("POST_TOWN")
    private String postTown;

    @JsonProperty("POSTCODE")
    private String postcode;

    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("COUNTRY_CODE")
    private String countryCode;

    @JsonProperty("COUNTRY_CODE_DESCRIPTION")
    private String countryCodeDescription;

    @JsonProperty("POSTAL_ADDRESS_CODE")
    private String postalAddressCode;
}