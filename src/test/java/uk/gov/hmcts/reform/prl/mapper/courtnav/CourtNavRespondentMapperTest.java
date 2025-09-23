package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavRespondent;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

// 1. Use SpringExtension to enable Spring testing features
@ExtendWith(SpringExtension.class)
// 2. Point to our custom, minimal configuration class
@ContextConfiguration(classes = CourtNavRespondentMapperTest.TestConfig.class)
public class CourtNavRespondentMapperTest {

    // 3. Define a static nested configuration class for this test
    @Configuration
    // 4. Tell this mini-context to scan ONLY the package where our mappers live
    @ComponentScan(basePackages = "uk.gov.hmcts.reform.prl.mapper.courtnav")
    public static class TestConfig {

    }

    @Autowired
    private CourtNavRespondentMapper courtNavRespondentMapper;

    @Test
    public void shouldMapFullRespondentDetailsCorrectly() {
        CourtNavRespondent input = CourtNavRespondent.builder()
            .firstName("John")
            .lastName("Smith")
            .previousName("Jon")
            .dateOfBirth(new CourtNavDate(1, 2, 1980))
            .email("john@example.com")
            .phoneNumber("0123456789")
            .address(CourtNavAddress.builder()
                                   .addressLine1("1 Main St")
                                   .postTown("Town")
                                   .postCode("TS1 1AA")
                                   .build())
            .respondentLivesWithApplicant(true)
            .build();

        PartyDetails result = courtNavRespondentMapper.map(input);

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
    public void shouldHandleNullContactDetailsSafely() {
        CourtNavRespondent input = CourtNavRespondent.builder()
            .firstName("Anon")
            .lastName("Unknown")
            .respondentLivesWithApplicant(false)
            .build();

        PartyDetails result = courtNavRespondentMapper.map(input);

        assertEquals("Anon", result.getFirstName());
        assertNull(result.getDateOfBirth());
        assertEquals(YesOrNo.No, result.getIsCurrentAddressKnown());
        assertEquals(YesOrNo.No, result.getCanYouProvideEmailAddress());
        assertEquals(YesOrNo.No, result.getCanYouProvidePhoneNumber());
        assertEquals(YesOrNo.No, result.getRespondentLivedWithApplicant());
    }
}
