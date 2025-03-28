package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WhoDoesTheChildLiveWithTest {

    @Test
    void shouldSetAllFieldsCorrectly() {
        Address address = Address.builder()
            .addressLine1("123 Example Street")
            .postCode("AB12 3CD")
            .build();

        WhoDoesTheChildLiveWith result = WhoDoesTheChildLiveWith.builder()
            .partyId("  abc123  ")
            .partyFullName("  John Doe  ")
            .partyType(PartyTypeEnum.APPLICANT)
            .childAddress(address)
            .build();

        assertThat(result.getPartyId()).isEqualTo("abc123");
        assertThat(result.getPartyFullName()).isEqualTo("John Doe");
        assertThat(result.getPartyType()).isEqualTo(PartyTypeEnum.APPLICANT);
        assertThat(result.getChildAddress()).isEqualTo(address);
    }

    @Test
    void shouldNullifyBlankPartyIdAndFullName() {
        WhoDoesTheChildLiveWith result = WhoDoesTheChildLiveWith.builder()
            .partyId("   ")
            .partyFullName("")
            .partyType(PartyTypeEnum.RESPONDENT)
            .childAddress(null)
            .build();

        assertThat(result.getPartyId()).isNull();
        assertThat(result.getPartyFullName()).isNull();
        assertThat(result.getPartyType()).isEqualTo(PartyTypeEnum.RESPONDENT);
        assertThat(result.getChildAddress()).isNull();
    }

    @Test
    void shouldPreserveNullInputs() {
        WhoDoesTheChildLiveWith result = WhoDoesTheChildLiveWith.builder()
            .partyId(null)
            .partyFullName(null)
            .partyType(null)
            .childAddress(null)
            .build();

        assertThat(result.getPartyId()).isNull();
        assertThat(result.getPartyFullName()).isNull();
        assertThat(result.getPartyType()).isNull();
        assertThat(result.getChildAddress()).isNull();
    }
}

