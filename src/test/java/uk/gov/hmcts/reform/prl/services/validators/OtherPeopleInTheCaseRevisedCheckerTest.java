package uk.gov.hmcts.reform.prl.services.validators;

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
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class OtherPeopleInTheCaseRevisedCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    OtherPeopleInTheCaseRevisedChecker otherPeopleInTheCaseChecker;

    @Test
    void whenNoCaseDataThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(otherPeopleInTheCaseChecker.isStarted(caseData));

    }

    @Test
    void whenNoCaseDataThenIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(otherPeopleInTheCaseChecker.isFinished(caseData));

    }

    @Test
    void whenNoCaseDataThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(otherPeopleInTheCaseChecker.hasMandatoryCompleted(caseData));

    }

    @Test
    void whenMinimalRelevantCaseDataThenIsStartedReturnsTrue() {
        PartyDetails other = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedOther = Element.<PartyDetails>builder().value(other).build();
        List<Element<PartyDetails>> otherList = Collections.singletonList(wrappedOther);

        CaseData caseData = CaseData.builder()
            .otherPartyInTheCaseRevised(otherList)
            .build();

        assertTrue(otherPeopleInTheCaseChecker.isStarted(caseData));
    }

    @Test
    void whenNoRelevantCaseDataThenIsStartedReturnsFalse() {
        PartyDetails other = null;
        Element<PartyDetails> wrappedOther = Element.<PartyDetails>builder().value(other).build();
        List<Element<PartyDetails>> otherList = Collections.singletonList(wrappedOther);

        CaseData caseData = CaseData.builder()
            .otherPartyInTheCaseRevised(otherList)
            .build();

        assertFalse(otherPeopleInTheCaseChecker.isStarted(caseData));
    }

    @Test
    void whenCompletePartyDetailsThenValidationReturnsTrue() {

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
            .liveInRefuge(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
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
    void whenCompletePartyDetailsButMissingOtherPersonRelationshipThenValidationReturnsFalse() {

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("firstName")
            .lastName("lastName")
            .gender(Gender.male)
            .isDateOfBirthKnown(YesOrNo.No)
            .dateOfBirth(LocalDate.of(1989, 10, 20))
            .isPlaceOfBirthKnown(YesOrNo.No)
            .isCurrentAddressKnown(YesOrNo.No)
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
    void whenCompletePartyDetailsButMissingOtherPersonRelationshipThenValidationReturnsFalseNoDetailsKnown() {

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("firstName")
            .lastName("lastName")
            .gender(Gender.male)
            .dateOfBirth(LocalDate.of(1989, 10, 20))
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
    void whenIncompleteAddressDataThenVerificationReturnsFalse() {
        Address address = Address.builder()
            .addressLine2("Test")
            .country("UK")
            .build();

        assertFalse(otherPeopleInTheCaseChecker.verifyAddressCompleted(address));
    }


    @Test
    void whenOtherPeopleInTheCasePresentExceptPlaceOfBirth() {

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
    void ifEmptyListOfPartyDetailsThenFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder()
            .otherPartyInTheCaseRevised(Collections.emptyList())
            .build();

        assertFalse(otherPeopleInTheCaseChecker.isFinished(caseData));
    }

    @Test
    void whenCompletePartyDetailsThenFinishedReturnsTrue() {

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
            .liveInRefuge(YesOrNo.No)
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
    void whenCompletePartyDetailsThenFinishedReturnsTrueLiveInRefugeNotPresent() {

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

        assertFalse(otherPeopleInTheCaseChecker.isFinished(caseData));

    }

    @Test
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(otherPeopleInTheCaseChecker.getDefaultTaskState(CaseData.builder().build()));
    }


}
