package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonRelationshipToChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class OtherPeopleInTheCaseMapperTest {

    @InjectMocks
    OtherPeopleInTheCaseMapper otherPeopleInTheCaseMapper;
    @Mock
    AddressMapper addressMapper;
    List<Element<PartyDetails>> otherPeopleInTheCase;
    List<Element<OtherPersonRelationshipToChild>> otherPersonRelationshipToChild;
    OtherPersonRelationshipToChild otherPersonRelationship;
    PartyDetails partyDetails;
    Address address;

    @Before
    public void setUp() {
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
    public void testOtherPeopleMapperEmptyCheck() {
        otherPeopleInTheCase = Collections.emptyList();
        assertTrue(otherPeopleInTheCaseMapper.map(otherPeopleInTheCase).isEmpty());

    }

    @Test
    public void testOtherPeopleMapperWithAllFields() {
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
    public void testOtherPeopleMapperWithSomeFields() {
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
}
