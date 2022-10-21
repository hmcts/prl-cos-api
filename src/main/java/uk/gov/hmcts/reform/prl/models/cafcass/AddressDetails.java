package uk.gov.hmcts.reform.prl.models.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;


@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class AddressDetails implements Serializable {
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
