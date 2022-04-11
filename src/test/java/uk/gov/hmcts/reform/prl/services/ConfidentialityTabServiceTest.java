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
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.Fl401ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.OtherPersonConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class ConfidentialityTabServiceTest {

    @InjectMocks
    ConfidentialityTabService confidentialityTabService;

    @Mock
    ObjectMapper objectMapper;

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

    @Test
    public void testChildAndPartyConfidentialDetails() {

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
        Element<Child> child1 = Element.<Child>builder().value(
            child).build();

        List<Element<Child>> listOfChild = List.of(
            child1
        );

        Element<PartyDetails> partyDetailsFirstRec = Element.<PartyDetails>builder().value(
            partyDetails1).build();
        Element<PartyDetails> partyDetailsSecondRec = Element.<PartyDetails>builder().value(
            partyDetails2).build();
        List<Element<PartyDetails>> listOfPartyDetails = List.of(
            partyDetailsFirstRec,
            partyDetailsSecondRec
        );
        CaseData caseData = CaseData.builder().applicants(listOfPartyDetails).children(listOfChild).caseTypeOfApplication(C100_CASE_TYPE).build();
        Map<String, Object> stringObjectMap = confidentialityTabService.updateConfidentialityDetails(caseData);

        assertTrue(stringObjectMap.containsKey("applicantsConfidentialDetails"));
        assertTrue(stringObjectMap.containsKey("childrenConfidentialDetails"));

    }

    @Test
    public void testFl401ChildConfidentialDetails() {
        ChildrenLiveAtAddress child = ChildrenLiveAtAddress.builder()
            .childFullName("Test")
            .keepChildrenInfoConfidential(YesOrNo.Yes)
            .build();

        List<ChildrenLiveAtAddress> listOfChildren = Collections.singletonList(child);
        List<Element<Fl401ChildConfidentialityDetails>> expectedOutput = List.of(
            Element.<Fl401ChildConfidentialityDetails>builder()
                .value(Fl401ChildConfidentialityDetails
                           .builder()
                           .fullName("Test")
                           .build()).build()
        );

        assertEquals(
            expectedOutput,
            confidentialityTabService.getFl401ChildrenConfidentialDetails(listOfChildren)
        );
    }

    @Test
    public void testChildAndPartyConfidentialDetailsFl401() {

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


        ChildrenLiveAtAddress child = ChildrenLiveAtAddress.builder()
            .childFullName("Test")
            .keepChildrenInfoConfidential(YesOrNo.Yes)
            .build();

        Element<ChildrenLiveAtAddress> child1 = Element.<ChildrenLiveAtAddress>builder().value(
            child).build();

        List<Element<ChildrenLiveAtAddress>> listOfChild = List.of(
            child1
        );

        Home home = Home.builder()
            .children(listOfChild)
            .build();

        CaseData caseData = CaseData.builder().applicantsFL401(partyDetails1).home(home).caseTypeOfApplication(FL401_CASE_TYPE).build();
        Map<String, Object> stringObjectMap = confidentialityTabService.updateConfidentialityDetails(caseData);

        assertTrue(stringObjectMap.containsKey("applicantsConfidentialDetails"));
        assertTrue(stringObjectMap.containsKey("fl401ChildrenConfidentialDetails"));

    }

}
