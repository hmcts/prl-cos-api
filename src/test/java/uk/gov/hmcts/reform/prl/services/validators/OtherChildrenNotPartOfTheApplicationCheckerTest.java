package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherChildrenNotInTheCase;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.Gender.other;

@ExtendWith(MockitoExtension.class)
class OtherChildrenNotPartOfTheApplicationCheckerTest {

    @Mock
    TaskErrorService taskErrorService;


    @InjectMocks
    OtherChildrenNotPartOfTheApplicationChecker childChecker;

    @Test
    void whenNoCaseDataPresentThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(childChecker.isStarted(caseData));
    }

    @Test
    void whenEmptyChildDataPresentThenIsStartedReturnsFalse() {
        OtherChildrenNotInTheCase child = OtherChildrenNotInTheCase.builder().build();
        Element<OtherChildrenNotInTheCase> wrappedChildren =
            Element.<OtherChildrenNotInTheCase>builder().value(child).build();
        List<Element<OtherChildrenNotInTheCase>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().childrenNotInTheCase(listOfChildren).build();

        assertFalse(childChecker.isStarted(caseData));
    }

    @Test
    void whenSomeChildDataPresentThenIsStartedReturnsTrue() {

        CaseData caseData = CaseData.builder()
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();

        assertTrue(childChecker.isStarted(caseData));
    }

    @Test
    void whenSomeChildDataPresentThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder()
            .childrenNotPartInTheCaseYesNo(YesOrNo.No)
            .build();

        assertFalse(childChecker.isStarted(caseData));
    }

    @Test
    void whenNoCaseDataPresentThenIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(childChecker.isFinished(caseData));
    }

    @Test
    void whenSomeChildDataPresentThenIsFinishedReturnsFalse() {
        ChildDetailsRevised child = ChildDetailsRevised.builder().firstName("Test").lastName("Name").build();
        Element<ChildDetailsRevised> wrappedChildren = Element.<ChildDetailsRevised>builder().value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().newChildDetails(listOfChildren).build();

        assertFalse(childChecker.isFinished(caseData));
    }

    @Test
    void whenAllChildDataPresentThenIsFinishedReturnsTrue() {
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


        assertTrue(childChecker.isFinished(caseData));
    }


    @Test
    void whenAllChildDataPresentThenIsFinishedReturnsTrueWithisDateOfBirthKnown() {
        OtherChildrenNotInTheCase child = OtherChildrenNotInTheCase.builder()
            .firstName("Test")
            .lastName("Name")
            .isDateOfBirthKnown(YesOrNo.No)
            .gender(other)
            .otherGender("dfs")
            .build();

        Element<OtherChildrenNotInTheCase> wrappedChildren =
            Element.<OtherChildrenNotInTheCase>builder().value(child).build();
        List<Element<OtherChildrenNotInTheCase>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .childrenNotInTheCase(listOfChildren)
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();


        assertTrue(childChecker.isFinished(caseData));
    }


    @Test
    void whenAllChildDataPresentThenIsFinishedReturnsFalse() {
        OtherChildrenNotInTheCase child = OtherChildrenNotInTheCase.builder()
            .firstName("Test")
            .isDateOfBirthKnown(YesOrNo.No)
            .gender(other)
            .otherGender("dfs")
            .build();

        Element<OtherChildrenNotInTheCase> wrappedChildren =
            Element.<OtherChildrenNotInTheCase>builder().value(child).build();
        List<Element<OtherChildrenNotInTheCase>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .childrenNotInTheCase(listOfChildren)
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();


        assertFalse(childChecker.isFinished(caseData));
    }


    @Test
    void whenAllChildDataPresentThenIsFinishedReturnsFalseWithNull() {

        CaseData caseData = CaseData.builder()
            .childrenNotInTheCase(null)
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();
        assertTrue(childChecker.isFinished(caseData));
    }

    @Test
    void whenAllChildDataPresentThenIsFinishedReturnsFalseWithNullYes() {

        CaseData caseData = CaseData.builder()
            .childrenNotInTheCase(null)
            .childrenNotPartInTheCaseYesNo(null)
            .build();
        assertFalse(childChecker.isFinished(caseData));
    }

    @Test
    void whenvalidateOtherChildrenNotInTheCaseReturnsTrue() {
        OtherChildrenNotInTheCase child = OtherChildrenNotInTheCase.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(female)
            .build();

        Element<OtherChildrenNotInTheCase> wrappedChildren = Element.<OtherChildrenNotInTheCase>builder().value(child).build();
        List<Element<OtherChildrenNotInTheCase>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .childrenNotInTheCase(listOfChildren)
            .build();

        assertTrue(childChecker.validateOtherChildrenNotInTheCase(caseData));
    }


    @Test
    void whenAllChildDataPresentThenIsFinishedReturnsTrueWithGenderOther() {
        OtherChildrenNotInTheCase child = OtherChildrenNotInTheCase.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(other)
            .otherGender("unknow")
            .build();

        Element<OtherChildrenNotInTheCase> wrappedChildren =
            Element.<OtherChildrenNotInTheCase>builder().value(child).build();
        List<Element<OtherChildrenNotInTheCase>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .childrenNotInTheCase(listOfChildren)
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();
        assertTrue(childChecker.isFinished(caseData));
    }

    @Test
    void whenNoCaseDataPresentThenHasMandatoryCompletedReturnsTrue() {
        CaseData caseData = CaseData.builder().build();
        assertFalse(childChecker.hasMandatoryCompleted(caseData));
    }


    @Test
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(childChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
