package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.Gender.FEMALE;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.ANOTHER_PERSON;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.RESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.FATHER;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.SPECIAL_GUARDIAN;

@RunWith(MockitoJUnitRunner.class)
public class ChildCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    ChildChecker childChecker;

    @Test
    public void whenNoCaseDataPresentThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assert !childChecker.isStarted(caseData);

    }

    @Test
    public void whenEmptyChildDataPresentThenIsStartedReturnsFalse() {
        Child child = Child.builder().build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren).build();

        assert !childChecker.isStarted(caseData);
    }

    @Test
    public void whenSomeChildDataPresentThenIsStartedReturnsTrue() {
        Child child = Child.builder().firstName("Test").lastName("Name").build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren).build();

        assert childChecker.isStarted(caseData);

    }

    @Test
    public void whenNoCaseDataPresentThenIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assert !childChecker.isFinished(caseData);

    }

    @Test
    public void whenEmptyChildDataPresentThenIsFinishedReturnsFalse() {
        Child child = Child.builder().build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren).build();

        assert !childChecker.isFinished(caseData);


    }

    @Test
    public void whenSomeChildDataPresentThenIsFinishedReturnsFalse() {
        Child child = Child.builder().firstName("Test").lastName("Name").build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren).build();

        assert !childChecker.isFinished(caseData);

    }

    @Test
    public void whenAllChildDataPresentThenIsFinishedReturnsTrue() {
        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(FEMALE)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(SPECIAL_GUARDIAN)
            .respondentsRelationshipToChild(FATHER)
            .childLiveWith(Collections.singletonList(RESPONDENT))
            .childrenKnownToLocalAuthority(YesNoDontKnow.YES)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.YES)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .children(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.YES)
            .childrenKnownToLocalAuthorityTextArea("TestString")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.DONT_KNOW)
            .build();


        assert childChecker.isFinished(caseData);

    }

    @Test
    public void whenAllChildDataPresentAndChoosenAnotherPersonNotListedWithNoPersonDetails() {
        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(FEMALE)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(SPECIAL_GUARDIAN)
            .respondentsRelationshipToChild(FATHER)
            .childLiveWith(Collections.singletonList(ANOTHER_PERSON))
            .childrenKnownToLocalAuthority(YesNoDontKnow.YES)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.YES)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren).childrenKnownToLocalAuthority(YesNoDontKnow.YES)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.YES)
            .build();

        assert !childChecker.isFinished(caseData);
    }

    @Test
    public void whenAllChildDataPresentAndChoosenAnotherPersonNotListedWithPersonDetails() {

        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();

        OtherPersonWhoLivesWithChild personWhoLivesWithChild = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.YES).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();

        Element<OtherPersonWhoLivesWithChild> wrappedList = Element.<OtherPersonWhoLivesWithChild>builder().value(personWhoLivesWithChild).build();
        List<Element<OtherPersonWhoLivesWithChild>> listOfOtherPersonsWhoLivedWithChild = Collections.singletonList(wrappedList);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(FEMALE)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(SPECIAL_GUARDIAN)
            .respondentsRelationshipToChild(FATHER)
            .childLiveWith(Collections.singletonList(ANOTHER_PERSON))
            .childrenKnownToLocalAuthority(YesNoDontKnow.YES)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.YES)
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren).childrenKnownToLocalAuthority(YesNoDontKnow.YES)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.YES)
            .build();

        assert childChecker.isFinished(caseData);
    }

    @Test
    public void whenParentalResChildDataNotPresentAndChoosenAnotherPersonNotListedWithPersonDetails() {

        Address address = Address.builder()
            .postTown("London")
            .build();

        OtherPersonWhoLivesWithChild personWhoLivesWithChild = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.YES).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();

        Element<OtherPersonWhoLivesWithChild> wrappedList = Element.<OtherPersonWhoLivesWithChild>builder().value(personWhoLivesWithChild).build();
        List<Element<OtherPersonWhoLivesWithChild>> listOfOtherPersonsWhoLivedWithChild = Collections.singletonList(wrappedList);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(FEMALE)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(SPECIAL_GUARDIAN)
            .respondentsRelationshipToChild(FATHER)
            .childLiveWith(Collections.singletonList(ANOTHER_PERSON))
            .childrenKnownToLocalAuthority(YesNoDontKnow.YES)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.YES)
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren).childrenKnownToLocalAuthority(YesNoDontKnow.YES)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.YES)
            .build();

        assert !childChecker.isFinished(caseData);
    }


    @Test
    public void whenAllChildDataPresentAndChoosenAnotherPersonNotListedWithPersonDetailsNotAvailableInSecondRow() {

        Address address = Address.builder()
            .postTown("London")
            .build();

        OtherPersonWhoLivesWithChild personWhoLivesWithChildFirstRow = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.YES).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();

        OtherPersonWhoLivesWithChild personWhoLivesWithChildSecondRow = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.YES).relationshipToChildDetails("test")
            .firstName("").lastName("test Last Name").address(address).build();

        Element<OtherPersonWhoLivesWithChild> firstWrappedList = Element.<OtherPersonWhoLivesWithChild>builder()
                                                                        .value(personWhoLivesWithChildFirstRow).build();
        Element<OtherPersonWhoLivesWithChild> secondWrappedList = Element.<OtherPersonWhoLivesWithChild>builder()
                                                                       .value(personWhoLivesWithChildSecondRow).build();
        List<Element<OtherPersonWhoLivesWithChild>> listOfOtherPersonsWhoLivedWithChild = Arrays.asList(
                                                                                                  firstWrappedList,
                                                                                                  secondWrappedList);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(FEMALE)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(SPECIAL_GUARDIAN)
            .respondentsRelationshipToChild(FATHER)
            .childLiveWith(Collections.singletonList(ANOTHER_PERSON))
            .childrenKnownToLocalAuthority(YesNoDontKnow.YES)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.YES)
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren).childrenKnownToLocalAuthority(YesNoDontKnow.YES)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.YES)
            .build();

        assert !childChecker.isFinished(caseData);
    }


}
