package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.OtherPersonConfidentialityDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ConfidentialityTabServiceTest {

    @InjectMocks
    ConfidentialityTabService confidentialityTabService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    CoreCaseDataService coreCaseDataService;

    Address address;
    PartyDetails partyDetails1;
    PartyDetails partyDetails2;

    @Before
    public void setUp() {

        address = Address.builder()
            .addressLine1("AddressLine1")
            .postTown("Xyz town")
            .postCode("AB1 2YZ")
            .build();
    }

    @Test
    public void testApplicantConfidentialDetails() {

        partyDetails1 = PartyDetails.builder()
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
            .isEmailAddressConfidential(YesOrNo.Yes)
            .build();

        partyDetails2 = PartyDetails.builder()
            .firstName("ABC 2")
            .lastName("XYZ 2")
            .dateOfBirth(LocalDate.of(2000, 01, 01))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isEmailAddressConfidential(YesOrNo.No)
            .phoneNumber("12345678900")
            .email("abc2@xyz.com")
            .build();
        List<Element<ApplicantConfidentialityDetails>> expectedOutput = List
            .of(Element.<ApplicantConfidentialityDetails>builder()
                    .value(ApplicantConfidentialityDetails.builder()
                               .firstName("ABC 1")
                               .lastName("XYZ 2")
                               .email("abc1@xyz.com")
                               .phoneNumber("09876543211")
                               .address(address)
                               .build()).build());
        assertEquals(
            expectedOutput,
            confidentialityTabService.getConfidentialApplicantDetails(List.of(partyDetails1, partyDetails2))
        );

    }

    @Test
    public void testChildConfidentialDetails() {
        OtherPersonWhoLivesWithChild personWhoLivesWithChild1 = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails("test")
            .firstName("Confidential First Name").lastName("Confidential Last Name").address(address).build();
        OtherPersonWhoLivesWithChild personWhoLivesWithChild2 = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.No).relationshipToChildDetails("test")
            .firstName("Nonconfidential test First Name").lastName("Nonconfidential test Last Name")
            .address(address).build();

        Element<OtherPersonWhoLivesWithChild> otherPersonElement1 = Element.<OtherPersonWhoLivesWithChild>builder().value(
            personWhoLivesWithChild1).build();
        Element<OtherPersonWhoLivesWithChild> otherPersonElement2 = Element.<OtherPersonWhoLivesWithChild>builder().value(
            personWhoLivesWithChild2).build();
        List<Element<OtherPersonWhoLivesWithChild>> listOfOtherPersonsWhoLivedWithChild = List.of(
            otherPersonElement1,
            otherPersonElement2
        );

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .build();


        List<Child> listOfChildren = Collections.singletonList(child);
        List<Element<ChildConfidentialityDetails>> expectedOutput = List.of(
            Element.<ChildConfidentialityDetails>builder()
                .value(ChildConfidentialityDetails
                           .builder()
                           .firstName("Test")
                           .lastName("Name")
                           .otherPerson(List.of(Element.<OtherPersonConfidentialityDetails>builder().value(
                               OtherPersonConfidentialityDetails.builder()
                                   .firstName("Confidential First Name")
                                   .lastName("Confidential Last Name")
                                   .relationshipToChildDetails("test")
                                   .address(address)
                                   .build()).build()))
                           .build()).build()
        );

        assertEquals(
            expectedOutput,
            confidentialityTabService.getChildrenConfidentialDetails(listOfChildren)
        );
    }
}
