package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ConfidentialityC8RefugeServiceTest {

    @InjectMocks
    ConfidentialityC8RefugeService confidentialityC8RefugeService;

    Address address;
    PartyDetails refugePartyDetails1;
    PartyDetails refugePartyDetails2;

    @Before
    public void setUp() {

        address = Address.builder()
            .addressLine1("AddressLine1")
            .postTown("Xyz town")
            .postCode("AB1 2YZ")
            .build();

        refugePartyDetails1 = PartyDetails.builder()
            .firstName("ABC 1")
            .lastName("XYZ 2")
            .dateOfBirth(LocalDate.of(2000, 01, 01))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("abc1@xyz.com")
            .phoneNumber("09876543211")
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .liveInRefuge(YesOrNo.Yes)
            .build();

        refugePartyDetails2 = PartyDetails.builder()
            .firstName("ABC 2")
            .lastName("XYZ 2")
            .dateOfBirth(LocalDate.of(2000, 01, 01))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isEmailAddressConfidential(YesOrNo.No)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("12345678900")
            .liveInRefuge(YesOrNo.Yes)
            .email("abc2@xyz.com")
            .build();
    }

    @Test
    public void testApplicantRefuge() {
        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        List<Element<PartyDetails>> partyDetailsWrappedList = Collections.singletonList(wrappedApplicants);

        HashMap<String, Object> updatedCaseData = new HashMap<>();
        confidentialityC8RefugeService
            .processForcePartiesConfidentialityIfLivesInRefugeForC100(
                Optional.of(partyDetailsWrappedList),
                updatedCaseData,
                "applicants",
                false);

        assertTrue(updatedCaseData.containsKey("applicants"));

    }

    @Test
    public void testRefugeNoApplicant() {
        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        List<Element<PartyDetails>> partyDetailsWrappedList = Collections.singletonList(wrappedApplicants);

        HashMap<String, Object> updatedCaseData = new HashMap<>();
        confidentialityC8RefugeService
            .processForcePartiesConfidentialityIfLivesInRefugeForC100(
                Optional.of(partyDetailsWrappedList),
                updatedCaseData,
                " ",
                false);

        assertTrue(updatedCaseData.containsKey(" "));

    }

    @Test
    public void testRefugeCleanup() {

        refugePartyDetails1 = refugePartyDetails1.toBuilder()
            .liveInRefuge(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        List<Element<PartyDetails>> partyDetailsWrappedList = Collections.singletonList(wrappedApplicants);

        HashMap<String, Object> updatedCaseData = new HashMap<>();
        confidentialityC8RefugeService
            .processForcePartiesConfidentialityIfLivesInRefugeForC100(
                Optional.of(partyDetailsWrappedList),
                updatedCaseData,
                " ",
                true);

        assertTrue(updatedCaseData.containsKey(" "));

    }


}
