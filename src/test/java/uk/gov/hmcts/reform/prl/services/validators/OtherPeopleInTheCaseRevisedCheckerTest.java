package uk.gov.hmcts.reform.prl.services.validators;

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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class OtherPeopleInTheCaseRevisedCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    OtherPeopleInTheCaseRevisedChecker otherPeopleInTheCaseChecker;

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(otherPeopleInTheCaseChecker.isStarted(caseData));

    }

    @Test
    public void whenNoCaseDataThenIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(otherPeopleInTheCaseChecker.isFinished(caseData));

    }

    @Test
    public void whenNoCaseDataThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(otherPeopleInTheCaseChecker.hasMandatoryCompleted(caseData));

    }

    @Test
    public void whenMinimalRelevantCaseDataThenIsStartedReturnsTrue() {
        PartyDetails other = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedOther = Element.<PartyDetails>builder().value(other).build();
        List<Element<PartyDetails>> otherList = Collections.singletonList(wrappedOther);

        CaseData caseData = CaseData.builder()
            .otherPartyInTheCaseRevised(otherList)
            .build();

        assertTrue(otherPeopleInTheCaseChecker.isStarted(caseData));
    }

    @Test
    public void whenNoRelevantCaseDataThenIsStartedReturnsFalse() {
        PartyDetails other = null;
        Element<PartyDetails> wrappedOther = Element.<PartyDetails>builder().value(other).build();
        List<Element<PartyDetails>> otherList = Collections.singletonList(wrappedOther);

        CaseData caseData = CaseData.builder()
            .otherPartyInTheCaseRevised(otherList)
            .build();

        assertFalse(otherPeopleInTheCaseChecker.isStarted(caseData));
    }

    @Test
    public void whenCompletePartyDetailsThenValidationReturnsTrue() {

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("firstName")
            .lastName("lastName")
            .gender(Gender.male)
            .isDateOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1989, 10, 20))
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .placeOfBirth("London")
            .isCurrentAddressKnown(YesOrNo.Yes)
            .address(Address.builder()
                         .addressLine1("add1")
                         .postCode("postcode")
                         .build())
            .isAddressConfidential(YesOrNo.Yes)
            .isAtAddressLessThan5Years(YesOrNo.No)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("email@email.com")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("02086656656")
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .build();

        assertTrue(otherPeopleInTheCaseChecker.validateMandatoryPartyDetailsForOtherPerson(partyDetails));

    }

    @Test
    public void whenCompletePartyDetailsButMissingOtherPersonRelationshipThenValidationReturnsFalse() {

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("firstName")
            .lastName("lastName")
            .gender(Gender.male)
            .isDateOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1989, 10, 20))
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .address(Address.builder()
                         .addressLine1("add1")
                         .postCode("postcode")
                         .build())
            .isAddressConfidential(YesOrNo.Yes)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .email("email@email.com")
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("02086656656")
            .build();

        assertFalse(otherPeopleInTheCaseChecker.validateMandatoryPartyDetailsForOtherPerson(partyDetails));

    }


    @Test
    public void whenIncompleteAddressDataThenVerificationReturnsFalse() {
        Address address = Address.builder()
            .addressLine2("Test")
            .country("UK")
            .build();

        assertFalse(otherPeopleInTheCaseChecker.verifyAddressCompleted(address));
    }


    @Test
    public void whenOtherPeopleInTheCasePresentExceptPlaceOfBirth() {

        OtherPersonRelationshipToChild personRelationshipToChild = OtherPersonRelationshipToChild.builder()
            .personRelationshipToChild("Test relationship")
            .build();

        Element<OtherPersonRelationshipToChild> wrappedList = Element.<OtherPersonRelationshipToChild>builder()
            .value(personRelationshipToChild).build();

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
            .dxNumber("123456")
            .gender(Gender.female)
            .lastName("lastName")
            .isAtAddressLessThan5Years(YesOrNo.Yes)
            .addressLivedLessThan5YearsDetails("Test")
            .previousName("testPreviousname")
            .isDateOfBirthKnown(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.No)
            .build();

        Element<PartyDetails> partyWrapped = Element.<PartyDetails>builder().value(partyDetails).build();
        List<Element<PartyDetails>> listOfParty = Collections.singletonList(partyWrapped);

        CaseData caseData = CaseData.builder().otherPartyInTheCaseRevised(listOfParty)
            .build();

        assertFalse(otherPeopleInTheCaseChecker.isFinished(caseData));
    }


    @Test
    public void ifEmptyListOfPartyDetailsThenFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder()
            .otherPartyInTheCaseRevised(Collections.emptyList())
            .build();

        assertFalse(otherPeopleInTheCaseChecker.isFinished(caseData));
    }

    @Test
    public void whenCompletePartyDetailsThenFinishedReturnsTrue() {

        OtherPersonRelationshipToChild personRelationshipToChild = OtherPersonRelationshipToChild.builder()
            .personRelationshipToChild("Test relationship")
            .build();

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("firstName")
            .lastName("lastName")
            .gender(Gender.male)
            .isDateOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1989, 10, 20))
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .placeOfBirth("London")
            .isCurrentAddressKnown(YesOrNo.Yes)
            .isAtAddressLessThan5Years(YesOrNo.No)
            .address(Address.builder()
                         .addressLine1("add1")
                         .postCode("postcode")
                         .build())
            .isAddressConfidential(YesOrNo.Yes)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .email("email@email.com")
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .phoneNumber("02086656656")
            .otherPersonRelationshipToChildren(List.of(element(personRelationshipToChild)))
            .build();

        CaseData caseData = CaseData.builder()
            .otherPartyInTheCaseRevised(List.of(element(partyDetails)))
            .build();

        assertTrue(otherPeopleInTheCaseChecker.isFinished(caseData));

    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(otherPeopleInTheCaseChecker.getDefaultTaskState(CaseData.builder().build()));
    }


}
