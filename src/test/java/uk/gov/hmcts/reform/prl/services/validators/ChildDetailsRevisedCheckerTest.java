package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.Gender.other;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;

@RunWith(MockitoJUnitRunner.class)
public class ChildDetailsRevisedCheckerTest {

    @Mock
    TaskErrorService taskErrorService;


    @InjectMocks
    ChildDetailsRevisedChecker childChecker;

    @Test
    public void whenNoCaseDataPresentThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertTrue(!childChecker.isStarted(caseData));
    }

    @Test
    public void whenEmptyChildDataPresentThenIsStartedReturnsFalse() {
        ChildDetailsRevised child = ChildDetailsRevised.builder().build();
        Element<ChildDetailsRevised> wrappedChildren = Element.<ChildDetailsRevised>builder().value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().newChildDetails(listOfChildren).build();

        assertTrue(!childChecker.isStarted(caseData));
    }

    @Test
    public void whenSomeChildDataPresentThenIsStartedReturnsTrue() {
        ChildDetailsRevised child = ChildDetailsRevised.builder().firstName("Test").lastName("Name").build();
        Element<ChildDetailsRevised> wrappedChildren = Element.<ChildDetailsRevised>builder().value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .newChildDetails(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("TestString")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.dontKnow)
            .build();

        assertTrue(childChecker.isStarted(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertTrue(!childChecker.isFinished(caseData));
    }

    @Test
    public void whenSomeChildDataPresentThenIsFinishedReturnsFalse() {
        ChildDetailsRevised child = ChildDetailsRevised.builder().firstName("Test").lastName("Name").build();
        Element<ChildDetailsRevised> wrappedChildren = Element.<ChildDetailsRevised>builder().value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().newChildDetails(listOfChildren).build();

        assertTrue(!childChecker.isFinished(caseData));
    }

    @Test
    public void whenAllChildDataPresentThenIsFinishedReturnsTrue() {
        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .whoDoesTheChildLiveWith(DynamicList.builder().build())
            .build();

        Element<ChildDetailsRevised> wrappedChildren = Element.<ChildDetailsRevised>builder().value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .newChildDetails(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("TestString")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.dontKnow)
            .build();


        assertTrue(childChecker.isFinished(caseData));
    }


    @Test
    public void whenAllChildDataPresentThenIsFinishedReturnsTrue1() {
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
            //.childrenSubjectOfChildProtectionPlan(YesNoDontKnow.dontKnow)
            .build();


        assertTrue(!childChecker.isFinished(caseData));
    }


    @Test
    public void whenAllChildDataPresentThenIsFinishedReturnsTrueWithGenderOther() {
        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(other)
            .otherGender("unknow")
            .whoDoesTheChildLiveWith(DynamicList.builder().build())
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
        assertTrue(childChecker.isFinished(caseData));
    }



    @Test
    public void whenAllChildDataPresentThenIsFinishedReturnsTrueWithGNull() {

        CaseData caseData = CaseData.builder()
            .newChildDetails(null)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("TestString")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.dontKnow)
            .build();
        assertTrue(!childChecker.isFinished(caseData));
    }


    @Test
    public void whenNoCaseDataPresentThenHasMandatoryCompletedReturnsTrue() {
        CaseData caseData = CaseData.builder().build();
        assertTrue(!childChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(childChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
