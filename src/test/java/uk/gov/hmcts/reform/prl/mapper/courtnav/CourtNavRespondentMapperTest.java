package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.RespondentDetails;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CourtNavRespondentMapperTest {

    private CourtNavRespondentMapper respondentMapper;

    @BeforeEach
    void setUp() {
        CourtNavAddressMapper addressMapper = new CourtNavAddressMapperImpl();
        respondentMapper = new CourtNavRespondentMapper(addressMapper);
    }

    @Test
    void shouldMapFullRespondentDetailsCorrectly() {
        RespondentDetails input = RespondentDetails.builder()
            .respondentFirstName("John")
            .respondentLastName("Smith")
            .respondentOtherNames("Jon")
            .respondentDateOfBirth(new CourtNavDate(1, 2, 1980))
            .respondentEmailAddress("john@example.com")
            .respondentPhoneNumber("0123456789")
            .respondentAddress(CourtNavAddress.builder()
                                   .addressLine1("1 Main St")
                                   .postTown("Town")
                                   .postCode("TS1 1AA")
                                   .build())
            .respondentLivesWithApplicant(true)
            .build();

        PartyDetails result = respondentMapper.mapRespondent(input);

        assertEquals("John", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("Jon", result.getPreviousName());
        assertEquals(LocalDate.of(1980, 2, 1), result.getDateOfBirth());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("0123456789", result.getPhoneNumber());
        assertEquals("1 Main St", result.getAddress().getAddressLine1());
        assertEquals(YesOrNo.Yes, result.getIsCurrentAddressKnown());
        assertEquals(YesOrNo.Yes, result.getCanYouProvideEmailAddress());
        assertEquals(YesOrNo.Yes, result.getCanYouProvidePhoneNumber());
        assertEquals(YesOrNo.Yes, result.getRespondentLivedWithApplicant());
        assertNotNull(result.getPartyId());
    }

    @Test
    void shouldHandleNullContactDetailsSafely() {
        RespondentDetails input = RespondentDetails.builder()
            .respondentFirstName("Anon")
            .respondentLastName("Unknown")
            .respondentLivesWithApplicant(false)
            .build();

        PartyDetails result = respondentMapper.mapRespondent(input);

        assertEquals("Anon", result.getFirstName());
        assertNull(result.getDateOfBirth());
        assertEquals(YesOrNo.No, result.getIsCurrentAddressKnown());
        assertEquals(YesOrNo.No, result.getCanYouProvideEmailAddress());
        assertEquals(YesOrNo.No, result.getCanYouProvidePhoneNumber());
        assertEquals(YesOrNo.No, result.getRespondentLivedWithApplicant());
    }
}

