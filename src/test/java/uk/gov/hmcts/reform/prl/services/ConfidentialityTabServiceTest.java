package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndApplicantRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.Fl401ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.OtherPersonConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Relations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

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
    public void testApplicantConfidentialDetailsWhenNull() {

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
            confidentialityTabService.getConfidentialApplicantDetails(List.of(partyDetails1))
        );

    }


    @Test
    public void testChildConfidentialDetailsV2() {

        ChildrenAndApplicantRelation childrenAndApplicantRelation = ChildrenAndApplicantRelation.builder()
            .applicantFullName("Test")
            .childFullName("Name").childAndApplicantRelation(RelationshipsEnum.other)
            .childAndApplicantRelationOtherDetails("dsdfs")
            .childLivesWith(YesOrNo.Yes)
            .build();

        Element<ChildrenAndApplicantRelation> childrenAndApplicantRelationElement =
            Element.<ChildrenAndApplicantRelation>builder().value(childrenAndApplicantRelation).build();
        List<Element<ChildrenAndApplicantRelation>> childrenAndApplicantRelationList = Collections.singletonList(childrenAndApplicantRelationElement);

        ChildrenAndOtherPeopleRelation childrenAndOtherPeopleRelation = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Ramesh Meripe")
            .childFullName("Cherry Meripe").childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();

        Element<ChildrenAndOtherPeopleRelation> wrappedChildrenAndOther =
            Element.<ChildrenAndOtherPeopleRelation>builder().value(childrenAndOtherPeopleRelation).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildrenAndOther = Collections.singletonList(wrappedChildrenAndOther);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Cherry")
            .lastName("Meripe")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren = Element.<ChildDetailsRevised>builder().value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Ramesh")
            .lastName("Meripe")
            .address(Address.builder()
                         .addressLine1("address")
                         .postTown("London")
                         .build())
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1999, 12, 10))
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .dxNumber("123456")
            .gender(Gender.female)
            .email("rame@gmail.com")
            .phoneNumber("07776817131")
            .previousName("testPreviousname")
            .isDateOfBirthKnown(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.No)
            .build();

        Element<PartyDetails> partyWrapped = Element.<PartyDetails>builder().value(partyDetails).build();
        List<Element<PartyDetails>> listOfParty = Collections.singletonList(partyWrapped);


        ChildrenAndRespondentRelation childrenAndRespondentRelation = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Test")
            .childFullName("Name").childAndRespondentRelation(RelationshipsEnum.other)
            .childLivesWith(YesOrNo.Yes)
            .childAndRespondentRelationOtherDetails("dsdfs")
            .build();

        Element<ChildrenAndRespondentRelation> childrenAndRespondentRelationElement =
            Element.<ChildrenAndRespondentRelation>builder().value(childrenAndRespondentRelation).build();
        List<Element<ChildrenAndRespondentRelation>> childrenAndRespondentRelationList =
            Collections.singletonList(childrenAndRespondentRelationElement);



        CaseData caseData = CaseData.builder()
            .taskListVersion("v2")
            .newChildDetails(listOfChildren)
                .relations(Relations.builder()
            .childAndRespondentRelations(childrenAndRespondentRelationList)
                        .childAndOtherPeopleRelations(listOfChildrenAndOther)
            .childAndApplicantRelations(childrenAndApplicantRelationList).build())
            .othersToNotify(listOfParty)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("TestString")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.dontKnow)
                .otherPartyInTheCaseRevised(List.of(partyWrapped))
            .build();


        assertNotNull(
            confidentialityTabService.getChildrenConfidentialDetails(caseData)
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
        CaseData caseData = CaseData.builder()
            .applicants(listOfPartyDetails)
            .children(listOfChild)
            .respondents(listOfPartyDetails)
            .caseTypeOfApplication(C100_CASE_TYPE).build();
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

        List<Element<ChildrenLiveAtAddress>> listOfChildren = Collections.singletonList(element(child));
        List<Element<Fl401ChildConfidentialityDetails>> expectedOutput = List.of(
            Element.<Fl401ChildConfidentialityDetails>builder()
                .value(Fl401ChildConfidentialityDetails
                           .builder()
                           .fullName("Test")
                           .build()).build()
        );

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .typeOfApplicationOrders(TypeOfApplicationOrders.builder()
            .orderType(List.of(FL401OrderTypeEnum.occupationOrder))
            .build())
            .home(Home.builder()
                      .children(listOfChildren)
                      .build())
            .build();


        assertEquals(
            expectedOutput,
            confidentialityTabService.getFl401ChildrenConfidentialDetails(caseData)
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

        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(TypeOfApplicationOrders.builder()
                                         .orderType(List.of(FL401OrderTypeEnum.occupationOrder))
                                         .build())
            .applicantsFL401(partyDetails1)
            .respondentsFL401(partyDetails1)
            .home(home)
            .caseTypeOfApplication(FL401_CASE_TYPE).build();
        Map<String, Object> stringObjectMap = confidentialityTabService.updateConfidentialityDetails(caseData);

        assertTrue(stringObjectMap.containsKey("applicantsConfidentialDetails"));
        assertTrue(stringObjectMap.containsKey("fl401ChildrenConfidentialDetails"));

    }

    @Test
    public void testApplicantConfidentialDetailsWhenNoApplicantsPresent() {

        CaseData caseData = CaseData.builder()
            .applicants(null)
            .children(null)
            .caseTypeOfApplication(C100_CASE_TYPE).build();
        Map<String, Object> stringObjectMap = confidentialityTabService.updateConfidentialityDetails(caseData);

        assertTrue(stringObjectMap.containsKey("applicantsConfidentialDetails"));
        assertTrue(stringObjectMap.containsKey("childrenConfidentialDetails"));
        assertEquals(Collections.EMPTY_LIST,stringObjectMap.get("applicantsConfidentialDetails"));
        assertEquals(Collections.EMPTY_LIST,stringObjectMap.get("childrenConfidentialDetails"));

    }

    @Test
    public void testApplicantConfidentialDetailsWhenNoFL401ApplicantsPresent() {

        ChildrenLiveAtAddress child = ChildrenLiveAtAddress.builder()
            .childFullName("Test")
            .keepChildrenInfoConfidential(YesOrNo.Yes)
            .build();

        Element<ChildrenLiveAtAddress> child1 = Element.<ChildrenLiveAtAddress>builder().value(
            child).build();

        List<Element<ChildrenLiveAtAddress>> listOfChild = List.of(
            child1
        );

        CaseData caseData = CaseData.builder()
            .applicantsFL401(null)
            .respondentsFL401(null)
            .typeOfApplicationOrders(TypeOfApplicationOrders.builder()
                                         .orderType(List.of(FL401OrderTypeEnum.occupationOrder))
                                         .build())
            .home(Home.builder()
                      .children(listOfChild)
                      .build())

            .caseTypeOfApplication(FL401_CASE_TYPE).build();
        Map<String, Object> stringObjectMap = confidentialityTabService.updateConfidentialityDetails(caseData);

        assertEquals(Collections.EMPTY_LIST,stringObjectMap.get("applicantsConfidentialDetails"));
        assertTrue(stringObjectMap.containsKey("fl401ChildrenConfidentialDetails"));

    }


}
