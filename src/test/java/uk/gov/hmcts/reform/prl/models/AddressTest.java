package uk.gov.hmcts.reform.prl.models;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {

    @Test
    void shouldBuildAddressWithCleanedValues() {
        Address address = Address.builder()
            .addressLine1("  123 Street  ")
            .addressLine2("  Apt 4B ")
            .addressLine3("  ")
            .postTown("  London ")
            .county("  Greater London ")
            .country(" UK ")
            .postCode(" AB1 2CD ")
            .build();

        assertThat(address.getAddressLine1()).isEqualTo("123 Street");
        assertThat(address.getAddressLine2()).isEqualTo("Apt 4B");
        assertThat(address.getAddressLine3()).isNull(); // blank becomes null
        assertThat(address.getPostTown()).isEqualTo("London");
        assertThat(address.getCounty()).isEqualTo("Greater London");
        assertThat(address.getCountry()).isEqualTo("UK");
        assertThat(address.getPostCode()).isEqualTo("AB1 2CD");
    }

    @Test
    void shouldHandleAllNullInputs() {
        Address address = Address.builder()
            .addressLine1(null)
            .addressLine2(null)
            .addressLine3(null)
            .postTown(null)
            .county(null)
            .country(null)
            .postCode(null)
            .build();

        assertThat(address.getAddressLine1()).isNull();
        assertThat(address.getAddressLine2()).isNull();
        assertThat(address.getAddressLine3()).isNull();
        assertThat(address.getPostTown()).isNull();
        assertThat(address.getCounty()).isNull();
        assertThat(address.getCountry()).isNull();
        assertThat(address.getPostCode()).isNull();
    }

    @Test
    void shouldIgnoreBlankStringsAndSetAsNull() {
        Address address = Address.builder()
            .addressLine1("   ")
            .addressLine2("")
            .postTown(" \n ")
            .build();

        assertThat(address.getAddressLine1()).isNull();
        assertThat(address.getAddressLine2()).isNull();
        assertThat(address.getPostTown()).isNull();
    }
}
