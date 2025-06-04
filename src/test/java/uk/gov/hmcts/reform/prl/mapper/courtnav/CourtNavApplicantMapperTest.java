package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ApplicantsDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreferredContactEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.Gender.male;

class CourtNavApplicantMapperTest {
    private CourtNavApplicantMapper courtNavApplicantMapper;

    @BeforeEach
    void setUp() {
        CourtNavAddressMapper addressMapper = Mappers.getMapper(CourtNavAddressMapper.class);
        courtNavApplicantMapper = new CourtNavApplicantMapper(addressMapper);
    }

    @Test
    void shouldMapFullApplicantDetailsCorrectly() {
        CourtNavAddress testAddress = CourtNavAddress.builder()
            .addressLine1("1 Main St")
            .postTown("Townsville")
            .postCode("TS1 1AA")
            .country("UK")
            .county("County")
            .build();

        ApplicantsDetails input = ApplicantsDetails.builder()
            .firstName("Jane")
            .lastName("Doe")
            .previousName("JD")
            .dateOfBirth(new CourtNavDate(1, 1, 1990))
            .gender(female)
            .address(testAddress)
            .email("jane@example.com")
            .phoneNumber("0123456789")
            .applicantPreferredContact(List.of(PreferredContactEnum.email))
            .applicantContactInstructions("Don't call after 5")
            .representativeFirstName("Solicitor")
            .representativeLastName("Smith")
            .solicitorTelephone("02070000000")
            .solicitorReference("REF123")
            .solicitorFirmName("Law & Co")
            .solicitorEmail("solicitor@example.com")
            .solicitorAddress(testAddress)
            .dxNumber("DX12345")
            .shareContactDetailsWithRespondent(false)
            .build();

        PartyDetails result = courtNavApplicantMapper.map(input);

        assertNotNull(result.getPartyId());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("JD", result.getPreviousName());
        assertEquals("1990-01-01", result.getDateOfBirth().toString());
        assertEquals(female, result.getGender());
        assertEquals("Don't call after 5", result.getApplicantContactInstructions());
        assertEquals(ContactPreferences.email, result.getContactPreferences());

        // Address mapping
        assertNotNull(result.getAddress());
        assertEquals("1 Main St", result.getAddress().getAddressLine1());

        // Confidential flags
        assertEquals(YesOrNo.Yes, result.getIsAddressConfidential());
        assertEquals(YesOrNo.Yes, result.getIsEmailAddressConfidential());
        assertEquals(YesOrNo.Yes, result.getIsPhoneNumberConfidential());

        // Solicitor block
        assertEquals("Law & Co", result.getSolicitorOrg().getOrganisationName());
        assertEquals("solicitor@example.com", result.getSolicitorEmail());
        assertEquals("DX12345", result.getDxNumber());
    }

    @Test
    void shouldHandleNullAddressAndContactFieldsSafely() {
        ApplicantsDetails input = ApplicantsDetails.builder()
            .firstName("Test")
            .lastName("User")
            .dateOfBirth(new CourtNavDate(12, 12, 2000))
            .gender(male)
            .shareContactDetailsWithRespondent(true)
            .build();

        PartyDetails result = courtNavApplicantMapper.map(input);

        assertEquals("Test", result.getFirstName());
        assertNull(result.getAddress());
        assertEquals(YesOrNo.No, result.getIsAddressConfidential());
        assertEquals(YesOrNo.No, result.getIsEmailAddressConfidential());
        assertEquals(YesOrNo.No, result.getIsPhoneNumberConfidential());
    }

    @Test
    void shouldSetOtherGenderCorrectly() {
        CourtNavAddress address = CourtNavAddress.builder()
            .addressLine1("1 Main St")
            .postTown("Townsville")
            .postCode("TS1 1AA")
            .country("UK")
            .county("County")
            .build();

        ApplicantsDetails input = ApplicantsDetails.builder()
            .firstName("Alex")
            .lastName("Taylor")
            .dateOfBirth(new CourtNavDate(5, 5, 1995))
            .gender(Gender.other)
            .otherGender("Non-binary")
            .address(address)
            .shareContactDetailsWithRespondent(false)
            .build();

        PartyDetails result = courtNavApplicantMapper.map(input);

        assertEquals(Gender.other, result.getGender());
        assertEquals("Non-binary", result.getOtherGender());
    }

}
