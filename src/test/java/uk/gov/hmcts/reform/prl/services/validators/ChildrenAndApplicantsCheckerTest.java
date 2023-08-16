package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndApplicantRelation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Relations;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class ChildrenAndApplicantsCheckerTest {


    @Mock
    TaskErrorService taskErrorService;

    @Mock
    EventsChecker eventsChecker;

    @InjectMocks
    ChildrenAndApplicantsChecker childrenAndApplicantsChecker;

    @Test
    public void whenNoCaseDataPresentThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build()).build();

        assertTrue(!childrenAndApplicantsChecker.isStarted(caseData));
    }

    @Test
    public void whenEmptyChildDataPresentThenIsStartedReturnsTrue() {
        ChildrenAndApplicantRelation child = ChildrenAndApplicantRelation.builder()
            .applicantFullName("Test")
            .childFullName("Name").childAndApplicantRelation(RelationshipsEnum.other)
            .childAndApplicantRelationOtherDetails("dsdfs")
            .childLivesWith(YesOrNo.Yes)
            .build();

        Element<ChildrenAndApplicantRelation> wrappedChildren = Element.<ChildrenAndApplicantRelation>builder().value(child).build();
        List<Element<ChildrenAndApplicantRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
            .childAndApplicantRelations(listOfChildren).build())
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();

        assertTrue(childrenAndApplicantsChecker.isStarted(caseData));
    }

    @Test
    public void whenSomeChildDataPresentThenIsStartedReturnsTrue() {

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build())
            .build();

        assertTrue(!childrenAndApplicantsChecker.isStarted(caseData));
    }



    @Test
    public void whenAllChildDataPresentThenIsFinishedReturnsTrueWithGenderOther() {
        ChildrenAndApplicantRelation child = ChildrenAndApplicantRelation.builder()
            .applicantFullName("Test")
            .childFullName("Name").childAndApplicantRelation(RelationshipsEnum.other)
            .childAndApplicantRelationOtherDetails("dfdsf")
            .childLivesWith(YesOrNo.Yes)
            .build();

        Element<ChildrenAndApplicantRelation> wrappedChildren = Element.<ChildrenAndApplicantRelation>builder().value(child).build();
        List<Element<ChildrenAndApplicantRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
                        .childAndApplicantRelations(listOfChildren).build())
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();
        assertTrue(childrenAndApplicantsChecker.isFinished(caseData));
    }

    @Test
    public void whenAllChildDataPresentThenIsFinishedReturnsFalseWithGenderOther() {
        ChildrenAndApplicantRelation child = ChildrenAndApplicantRelation.builder()
            .applicantFullName("Test")
            .childFullName("Name").childAndApplicantRelation(RelationshipsEnum.other)
            .childLivesWith(YesOrNo.Yes)
            .build();

        Element<ChildrenAndApplicantRelation> wrappedChildren = Element.<ChildrenAndApplicantRelation>builder().value(child).build();
        List<Element<ChildrenAndApplicantRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
                        .childAndApplicantRelations(listOfChildren).build()).childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();
        assertTrue(!childrenAndApplicantsChecker.isFinished(caseData));
    }


    @Test
    public void whenAllChildDataPresentThenIsFinishedReturnsFalse1() {
        ChildrenAndApplicantRelation child = ChildrenAndApplicantRelation.builder()
            .applicantFullName("Test")
            .childFullName("Name").childAndApplicantRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .build();

        Element<ChildrenAndApplicantRelation> wrappedChildren = Element.<ChildrenAndApplicantRelation>builder().value(child).build();
        List<Element<ChildrenAndApplicantRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
                        .childAndApplicantRelations(listOfChildren).build())
                .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();
        assertTrue(childrenAndApplicantsChecker.isFinished(caseData));
    }


    @Test
    public void whenAllChildDataPresentThenIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build())
            .build();
        assertTrue(!childrenAndApplicantsChecker.isFinished(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenHasMandatoryCompletedReturnsTrue() {
        CaseData caseData = CaseData.builder().build();
        assertTrue(!childrenAndApplicantsChecker.hasMandatoryCompleted(caseData));
    }


    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(childrenAndApplicantsChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
