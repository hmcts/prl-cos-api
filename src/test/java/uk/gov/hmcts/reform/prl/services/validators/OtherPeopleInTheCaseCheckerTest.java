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

@RunWith(MockitoJUnitRunner.class)
public class OtherPeopleInTheCaseCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    OtherPeopleInTheCaseChecker otherPeopleInTheCaseChecker;

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
            .othersToNotify(otherList)
            .build();

        assertTrue(otherPeopleInTheCaseChecker.isStarted(caseData));
    }

    @Test
    public void whenIncompleteCaseDataValidateMandatoryFieldsForOtherReturnsFalse() {

        OtherPersonRelationshipToChild personRelationshipToChild = OtherPersonRelationshipToChild.builder()
            .personRelationshipToChild("Test relationship")
            .build();

        Element<OtherPersonRelationshipToChild> wrappedList = Element.<OtherPersonRelationshipToChild>builder()
            .value(personRelationshipToChild).build();
        List<Element<OtherPersonRelationshipToChild>> otherPersonRelationChildList = Collections
            .singletonList(wrappedList);

        PartyDetails other = PartyDetails.builder()
            .firstName("TestName")
            .otherPersonRelationshipToChildren(otherPersonRelationChildList)
            .build();


        assertFalse(otherPeopleInTheCaseChecker.validateMandatoryPartyDetailsForOtherPerson(other));

    }

    @Test
    public void whenOtherPeopleInTheCasePresentExceptPlaceOfBirth() {

        OtherPersonRelationshipToChild personRelationshipToChild = OtherPersonRelationshipToChild.builder()
            .personRelationshipToChild("Test relationship")
            .build();

        Element<OtherPersonRelationshipToChild> wrappedList = Element.<OtherPersonRelationshipToChild>builder()
            .value(personRelationshipToChild).build();
        List<Element<OtherPersonRelationshipToChild>> otherPersonRelationTChildList = Collections
            .singletonList(wrappedList);

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Test")
            .address(Address.builder()
                         .addressLine1("address")
                         .postTown("London")
                         .build())
            .addressLivedLessThan5YearsDetails("yes")
            .dateOfBirth(LocalDate.of(1999, 12, 10))
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .dxNumber("123456")
            .gender(Gender.female)
            .lastName("lastName")
            .previousName("testPreviousname")
            .isDateOfBirthKnown(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.No)
            .otherPersonRelationshipToChildren(otherPersonRelationTChildList)
            .build();

        Element<PartyDetails> partyWrapped = Element.<PartyDetails>builder().value(partyDetails).build();
        List<Element<PartyDetails>> listOfParty = Collections.singletonList(partyWrapped);

        CaseData caseData = CaseData.builder().othersToNotify(listOfParty)
            .build();

        assertFalse(otherPeopleInTheCaseChecker.isFinished(caseData));
    }

    @Test
    public void whenOtherPeopleInTheCasePresentNotWithRelationshipToChild() {

        OtherPersonRelationshipToChild personRelationshipToChild = OtherPersonRelationshipToChild.builder()
            .personRelationshipToChild("Test relationship")
            .build();

        Element<OtherPersonRelationshipToChild> wrappedList = Element.<OtherPersonRelationshipToChild>builder()
                    .value(personRelationshipToChild).build();
        List<Element<OtherPersonRelationshipToChild>> otherPersonRelationTChildList = Collections
                                                                                        .singletonList(wrappedList);

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Test")
            .address(Address.builder()
                         .addressLine1("address")
                         .postTown("London")
                         .build())
            .addressLivedLessThan5YearsDetails("yes")
            .dateOfBirth(LocalDate.of(1999, 12, 10))
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .dxNumber("123456")
            .gender(Gender.female)
            .lastName("lastName")
            .otherPersonRelationshipToChildren(otherPersonRelationTChildList)
            .build();

        Element<PartyDetails> partyWrapped = Element.<PartyDetails>builder().value(partyDetails).build();
        List<Element<PartyDetails>> listOfParty = Collections.singletonList(partyWrapped);

        CaseData caseData = CaseData.builder().othersToNotify(listOfParty)
            .build();

        assertFalse(otherPeopleInTheCaseChecker.isFinished(caseData));
    }

}
