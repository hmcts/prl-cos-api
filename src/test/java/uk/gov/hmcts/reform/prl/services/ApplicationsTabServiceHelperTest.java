package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndApplicantRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherChildrenNotInTheCase;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.OtherPersonInTheCaseRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Relations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.THIS_INFORMATION_IS_CONFIDENTIAL;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationsTabServiceHelperTest {

    @InjectMocks
    ApplicationsTabServiceHelper applicationsTabService;

    @Mock
    ObjectMapper objectMapper;

    @Test
    public void testGetChildRevisedDetails() {

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren = Element.<ChildDetailsRevised>builder().value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .newChildDetails(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("TestString")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.dontKnow)
            .build();
        assertNotNull(applicationsTabService.getChildRevisedDetails(caseData));
    }

    @Test
    public void testGetChildRevisedDetailsWithoutChildDetails() {

        CaseData caseData = CaseData.builder()
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("TestString")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.dontKnow)
            .build();
        assertNotNull(applicationsTabService.getChildRevisedDetails(caseData));
    }

    @Test
    public void testGetChildDetailsRevised() {

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        assertNotNull(applicationsTabService.getChildDetailsRevised(child));
    }

    @Test
    public void testGetChildAndApplicantsRelationTable() {
        ChildrenAndApplicantRelation child = ChildrenAndApplicantRelation.builder()
            .applicantFullName("Test")
            .childFullName("Name").childAndApplicantRelation(RelationshipsEnum.other)
            .childAndApplicantRelationOtherDetails("dfdsf")
            .childLivesWith(YesOrNo.Yes)
            .build();

        Element<ChildrenAndApplicantRelation> wrappedChildren = Element.<ChildrenAndApplicantRelation>builder().value(child).build();
        List<Element<ChildrenAndApplicantRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().childAndApplicantRelations(listOfChildren).build())
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();

        assertNotNull(applicationsTabService.getChildAndApplicantsRelationTable(caseData));
    }

    @Test
    public void testGetChildAndApplicantsRelationTableWithoutChildRelation() {
        CaseData caseData = CaseData.builder()
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
                .relations(Relations.builder().build())
            .build();

        assertNotNull(applicationsTabService.getChildAndApplicantsRelationTable(caseData));
    }


    @Test
    public void testGetChildAndRespondentRelationsTable() {
        ChildrenAndRespondentRelation child = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Test")
            .childFullName("Name").childAndRespondentRelation(RelationshipsEnum.other)
            .childLivesWith(YesOrNo.Yes)
            .build();

        Element<ChildrenAndRespondentRelation> wrappedChildren = Element.<ChildrenAndRespondentRelation>builder().value(child).build();
        List<Element<ChildrenAndRespondentRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
            .childAndRespondentRelations(listOfChildren).build())
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();
        assertNotNull(applicationsTabService.getChildAndRespondentRelationsTable(caseData));
    }


    @Test
    public void testGetChildAndRespondentRelationsTableWithoutList() {

        CaseData caseData = CaseData.builder()
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
                .relations(Relations.builder().build())
            .build();
        assertNotNull(applicationsTabService.getChildAndRespondentRelationsTable(caseData));
    }


    @Test
    public void testGetChildAndOtherPeopleRelationsTable() {
        ChildrenAndOtherPeopleRelation child = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test")
            .childFullName("Name").childAndOtherPeopleRelation(RelationshipsEnum.other)
            .childLivesWith(YesOrNo.Yes)
            .childAndOtherPeopleRelationOtherDetails("Test")
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();

        Element<ChildrenAndOtherPeopleRelation> wrappedChildren =
            Element.<ChildrenAndOtherPeopleRelation>builder().value(child).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
            .childAndOtherPeopleRelations(listOfChildren).build())
            .build();
        assertNotNull(applicationsTabService.getChildAndOtherPeopleRelationsTable(caseData));
    }

    @Test
    public void testGetChildAndOtherPeopleRelationsTable1() {
        ChildrenAndOtherPeopleRelation child = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test")
            .childFullName("Name").childAndOtherPeopleRelation(RelationshipsEnum.other)
            .childLivesWith(YesOrNo.Yes)
            .childAndOtherPeopleRelationOtherDetails("Test")
            .isChildLivesWithPersonConfidential(YesOrNo.No)
            .build();

        Element<ChildrenAndOtherPeopleRelation> wrappedChildren =
            Element.<ChildrenAndOtherPeopleRelation>builder().value(child).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
            .childAndOtherPeopleRelations(listOfChildren).build())
            .build();
        assertNotNull(applicationsTabService.getChildAndOtherPeopleRelationsTable(caseData));
    }



    @Test
    public void testGetChildAndOtherPeopleRelationsTableWithoutList() {

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build())
            .build();
        assertNotNull(applicationsTabService.getChildAndOtherPeopleRelationsTable(caseData));
    }


    @Test
    public void testGetOtherChildNotInTheCaseTable() {
        OtherChildrenNotInTheCase child = OtherChildrenNotInTheCase.builder()
            .firstName("Test")
            .lastName("Name")
            .isDateOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(female)
            .build();

        Element<OtherChildrenNotInTheCase> wrappedChildren =
            Element.<OtherChildrenNotInTheCase>builder().value(child).build();
        List<Element<OtherChildrenNotInTheCase>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .childrenNotInTheCase(listOfChildren)
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();
        assertNotNull(applicationsTabService.getOtherChildNotInTheCaseTable(caseData));
    }

    @Test
    public void testGetOtherChildNotInTheCaseTableNo() {
        CaseData caseData = CaseData.builder()
            .childrenNotPartInTheCaseYesNo(YesOrNo.No)
            .build();
        assertNotNull(applicationsTabService.getOtherChildNotInTheCaseTable(caseData));
    }


    @Test
    public void testGetOtherChildNotInTheCaseTableWithoutList() {
        CaseData caseData = CaseData.builder()
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();
        assertNotNull(applicationsTabService.getOtherChildNotInTheCaseTable(caseData));
    }

    @Test
    public void testGetOtherPeopleInTheCaseRevisedTable() {

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Test")
            .address(Address.builder()
                         .addressLine1("address")
                         .postTown("London")
                         .build())
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1999, 12, 10))
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .isAtAddressLessThan5Years(YesOrNo.Yes)
            .addressLivedLessThan5YearsDetails("test")
            .isAtAddressLessThan5Years(YesOrNo.Yes)
            .addressLivedLessThan5YearsDetails("Test")
            .dxNumber("123456")
            .gender(Gender.female)
            .lastName("lastName")
            .previousName("testPreviousname")
            .isDateOfBirthKnown(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.No)
            .build();

        Element<PartyDetails> partyWrapped = Element.<PartyDetails>builder().value(partyDetails).build();
        List<Element<PartyDetails>> listOfParty = Collections.singletonList(partyWrapped);

        CaseData caseData = CaseData.builder().othersToNotify(listOfParty)
            .build();

        assertNotNull(applicationsTabService.getOtherPeopleInTheCaseRevisedTable(caseData));
    }

    @Test
    public void testGetOtherPeopleWhenOtherPartyInTheCaseRevisedIsNotEmpty() {

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Test")
            .address(Address.builder()
                         .addressLine1("address")
                         .postTown("London")
                         .build())
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1999, 12, 10))
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .isAtAddressLessThan5Years(YesOrNo.Yes)
            .addressLivedLessThan5YearsDetails("test")
            .isAtAddressLessThan5Years(YesOrNo.Yes)
            .addressLivedLessThan5YearsDetails("Test")
            .dxNumber("123456")
            .gender(Gender.female)
            .lastName("lastName")
            .previousName("testPreviousname")
            .isDateOfBirthKnown(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.No)
            .build();

        Element<PartyDetails> partyWrapped = Element.<PartyDetails>builder().value(partyDetails).build();
        List<Element<PartyDetails>> listOfParty = Collections.singletonList(partyWrapped);

        OtherPersonInTheCaseRevised otherPerson = OtherPersonInTheCaseRevised.builder()
            .firstName("Test")
            .address(Address.builder()
                         .addressLine1("address")
                         .postTown("London")
                         .build())
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1999, 12, 10))
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .isAtAddressLessThan5Years(YesOrNo.Yes)
            .addressLivedLessThan5YearsDetails("test")
            .isAtAddressLessThan5Years(YesOrNo.Yes)
            .addressLivedLessThan5YearsDetails("Test")
            .gender("Female")
            .lastName("lastName")
            .previousName("testPreviousname")
            .isDateOfBirthKnown(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.No)
            .build();

        Element<OtherPersonInTheCaseRevised> otherPersonWrapped = Element.<OtherPersonInTheCaseRevised>builder().value(otherPerson).build();
        List<Element<OtherPersonInTheCaseRevised>> listOfOtherPerson = Collections.singletonList(otherPersonWrapped);

        CaseData caseData = CaseData.builder()
            .othersToNotify(listOfParty)
            .otherPartyInTheCaseRevised(listOfParty)
            .build();

        when(objectMapper.convertValue(partyDetails, OtherPersonInTheCaseRevised.class)).thenReturn(otherPerson);
        assertNotNull(applicationsTabService.getOtherPeopleInTheCaseRevisedTable(caseData));
        assertEquals(listOfOtherPerson, applicationsTabService.getOtherPeopleInTheCaseRevisedTable(caseData));
    }

    @Test
    public void testGetOtherPeopleWhenOtherPartyInTheCaseRevisedIsNotEmptyAndGenderIsNotAdded() {

        PartyDetails partyDetails = PartyDetails.builder().firstName("Test").build();
        Element<PartyDetails> partyWrapped = Element.<PartyDetails>builder().value(partyDetails).build();
        List<Element<PartyDetails>> listOfParty = Collections.singletonList(partyWrapped);

        OtherPersonInTheCaseRevised otherPerson = OtherPersonInTheCaseRevised.builder().firstName("Test").build();
        Element<OtherPersonInTheCaseRevised> otherPersonWrapped = Element.<OtherPersonInTheCaseRevised>builder().value(otherPerson).build();
        List<Element<OtherPersonInTheCaseRevised>> listOfOtherPerson = Collections.singletonList(otherPersonWrapped);

        CaseData caseData = CaseData.builder()
            .othersToNotify(listOfParty)
            .otherPartyInTheCaseRevised(listOfParty)
            .build();

        when(objectMapper.convertValue(partyDetails, OtherPersonInTheCaseRevised.class)).thenReturn(otherPerson);
        assertNotNull(applicationsTabService.getOtherPeopleInTheCaseRevisedTable(caseData));
        assertEquals(listOfOtherPerson, applicationsTabService.getOtherPeopleInTheCaseRevisedTable(caseData));
    }

    @Test
    public void testGetOtherPeopleInTheCaseRevisedTableWithoutList() {

        CaseData caseData = CaseData.builder()
            .build();

        assertNotNull(applicationsTabService.getOtherPeopleInTheCaseRevisedTable(caseData));
    }


    @Test
    public void testMaskingPartyDetails() {

        Address address = Address.builder()
            .addressLine1("55 Test Street")
            .postTown("Town")
            .postCode("N12 3BH")
            .build();
        PartyDetails partyDetails1 = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(Gender.male)
            .address(address)
            .isAddressConfidential(YesOrNo.Yes)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .email("test@test.com")
            .phoneNumber("1234567890")
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        PartyDetails expectedPartDetails = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(Gender.male)
            .address(Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build())
            .isAddressConfidential(YesOrNo.Yes)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .email(THIS_INFORMATION_IS_CONFIDENTIAL)
            .phoneNumber("1234567890")
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();


        assertEquals(1, List.of(partyDetails1).size());
        assertEquals(
            List.of(expectedPartDetails),
            applicationsTabService.maskConfidentialDetails(List.of(partyDetails1))
        );
    }
}
