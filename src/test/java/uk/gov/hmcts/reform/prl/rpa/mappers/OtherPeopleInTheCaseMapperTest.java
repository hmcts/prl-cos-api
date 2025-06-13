package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonRelationshipToChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class OtherPeopleInTheCaseMapperTest {

    @InjectMocks
    OtherPeopleInTheCaseMapper otherPeopleInTheCaseMapper;
    @Mock
    AddressMapper addressMapper;
    List<Element<PartyDetails>> otherPeopleInTheCase;
    List<Element<OtherPersonRelationshipToChild>> otherPersonRelationshipToChild;
    OtherPersonRelationshipToChild otherPersonRelationship;
    PartyDetails partyDetails;
    Address address;

    @BeforeEach
    void setUp() {
        address = Address.builder()
            .addressLine1("55 Test Street")
            .postTown("Town")
            .postCode("N12 3BH")
            .build();

        otherPersonRelationship = OtherPersonRelationshipToChild.builder()
            .personRelationshipToChild("Father").build();
        Element<OtherPersonRelationshipToChild> otherPersonRelationshipToChildElement = Element
            .<OtherPersonRelationshipToChild>builder().value(otherPersonRelationship).build();
        otherPersonRelationshipToChild = Collections.singletonList(otherPersonRelationshipToChildElement);

    }


    @Test
    void testOtherPeopleMapperEmptyCheck() {
        otherPeopleInTheCase = Collections.emptyList();
        assertTrue(otherPeopleInTheCaseMapper.map(otherPeopleInTheCase).isEmpty());

    }

    @Test
    void testOtherPeopleMapperWithAllFields() {
        partyDetails = PartyDetails.builder().firstName("FirstName").lastName("LastName")
            .previousName("PreviousName").isDateOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1990, 8, 1)).gender(Gender.female)
            .otherGender("Female").canYouProvideEmailAddress(YesOrNo.Yes).email("Email")
            .canYouProvidePhoneNumber(YesOrNo.Yes).phoneNumber("23123123").isPlaceOfBirthKnown(YesOrNo.Yes)
            .placeOfBirth("London").isCurrentAddressKnown(YesOrNo.Yes).address(address).otherPersonRelationshipToChildren(
                otherPersonRelationshipToChild)
            .build();
        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder().value(partyDetails).build();
        otherPeopleInTheCase = Collections.singletonList(partyDetailsElement);
        assertNotNull(otherPeopleInTheCaseMapper.map(otherPeopleInTheCase));

    }

    @Test
    void testOtherPeopleMapperWithSomeFields() {
        partyDetails = PartyDetails.builder().firstName("FirstName").lastName("LastName")
            .isDateOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1990, 8, 1)).gender(Gender.female)
            .otherGender("Female").canYouProvideEmailAddress(YesOrNo.Yes).email("Email")
            .canYouProvidePhoneNumber(YesOrNo.Yes).phoneNumber("23123123").isPlaceOfBirthKnown(YesOrNo.Yes)
            .placeOfBirth("London").isCurrentAddressKnown(YesOrNo.No).otherPersonRelationshipToChildren(
                otherPersonRelationshipToChild)
            .build();
        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder().value(partyDetails).build();
        otherPeopleInTheCase = Collections.singletonList(partyDetailsElement);
        assertNotNull(otherPeopleInTheCaseMapper.map(otherPeopleInTheCase));
    }

    @Test
    void testChildrenMapperWithEmptyValues() {
        assertTrue(otherPeopleInTheCaseMapper.map(null).isEmpty());

    }
}
