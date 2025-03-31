package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderClassName = "LombokBuilder", builderMethodName = "internalBuilder")
public class Address {

    @JsonProperty("AddressLine1")
    private String addressLine1;
    @JsonProperty("AddressLine2")
    private String addressLine2;
    @JsonProperty("AddressLine3")
    private String addressLine3;
    @JsonProperty("PostTown")
    private String postTown;
    @JsonProperty("County")
    private String county;
    @JsonProperty("Country")
    private String country;
    @JsonProperty("PostCode")
    private String postCode;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String addressLine1;
        private String addressLine2;
        private String addressLine3;
        private String postTown;
        private String county;
        private String country;
        private String postCode;

        private static String clean(String value) {
            return (value != null && !value.trim().isEmpty()) ? value : null;
        }

        public Builder addressLine1(String addressLine1) {
            this.addressLine1 = clean(addressLine1);
            return this;
        }

        public Builder addressLine2(String addressLine2) {
            this.addressLine2 = clean(addressLine2);
            return this;
        }

        public Builder addressLine3(String addressLine3) {
            this.addressLine3 = clean(addressLine3);
            return this;
        }

        public Builder postTown(String postTown) {
            this.postTown = clean(postTown);
            return this;
        }

        public Builder county(String county) {
            this.county = clean(county);
            return this;
        }

        public Builder country(String country) {
            this.country = clean(country);
            return this;
        }

        public Builder postCode(String postCode) {
            this.postCode = clean(postCode);
            return this;
        }

        public Address build() {
            return new Address(addressLine1, addressLine2, addressLine3, postTown, county, country, postCode);
        }
    }
}

