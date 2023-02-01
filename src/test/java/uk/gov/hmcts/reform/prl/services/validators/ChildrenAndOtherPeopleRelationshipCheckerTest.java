package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class ChildrenAndOtherPeopleRelationshipCheckerTest {


    @Mock
    TaskErrorService taskErrorService;


    @InjectMocks
    ChildrenAndOtherPeopleInThisApplicationChecker otherPeopleInTheCaseChecker;

    @Test
    public void whenNoCaseDataPresentThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertTrue(!otherPeopleInTheCaseChecker.isStarted(caseData));
    }

    @Test
    public void whenEmptyChildDataPresentThenIsStartedReturnsFalse() {
        ChildrenAndOtherPeopleRelation child = ChildrenAndOtherPeopleRelation.builder().build();
        Element<ChildrenAndOtherPeopleRelation> wrappedChildren =
            Element.<ChildrenAndOtherPeopleRelation>builder().value(child).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().childAndOtherPeopleRelations(listOfChildren).build();

        assertTrue(!otherPeopleInTheCaseChecker.isStarted(caseData));
    }

    @Test
    public void whenSomeChildDataPresentThenIsStartedReturnsTrue() {

        CaseData caseData = CaseData.builder()
            .build();

        assertTrue(!otherPeopleInTheCaseChecker.isStarted(caseData));
    }


    @Test
    public void whenSomeChildDataPresentThenIsFinishedReturnsFalse() {
        ChildrenAndOtherPeopleRelation child = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test").childFullName("Name")
            .childAndOtherPeopleRelation(RelationshipsEnum.father).build();
        Element<ChildrenAndOtherPeopleRelation> wrappedChildren = Element.<ChildrenAndOtherPeopleRelation>builder().value(child).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().childAndOtherPeopleRelations(listOfChildren).build();

        assertTrue(!otherPeopleInTheCaseChecker.isFinished(caseData));
    }

    @Test
    public void whenAllChildDataPresentThenIsFinishedReturnsTrue() {
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
            .childAndOtherPeopleRelations(listOfChildren)
            .build();

        assertTrue(otherPeopleInTheCaseChecker.isFinished(caseData));
    }

    @Test
    public void whenvalidateOtherChildrenNotInTheCaseReturnsTrue() {
        ChildrenAndOtherPeopleRelation child = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test")
            .childFullName("Name").childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();

        Element<ChildrenAndOtherPeopleRelation> wrappedChildren = Element.<ChildrenAndOtherPeopleRelation>builder().value(child).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .childAndOtherPeopleRelations(listOfChildren)
            .build();

        assertTrue(otherPeopleInTheCaseChecker.isFinished(caseData));
    }


    @Test
    public void whenAllChildDataPresentThenIsFinishedReturnsTrueWithGenderOther() {
        ChildrenAndOtherPeopleRelation child = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test")
            .childFullName("Name").childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();

        Element<ChildrenAndOtherPeopleRelation> wrappedChildren = Element.<ChildrenAndOtherPeopleRelation>builder().value(child).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .childAndOtherPeopleRelations(listOfChildren)
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();
        assertTrue(otherPeopleInTheCaseChecker.isFinished(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenHasMandatoryCompletedReturnsTrue() {
        CaseData caseData = CaseData.builder().build();
        assertTrue(!otherPeopleInTheCaseChecker.hasMandatoryCompleted(caseData));
    }


    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(otherPeopleInTheCaseChecker.getDefaultTaskState());
    }
}
