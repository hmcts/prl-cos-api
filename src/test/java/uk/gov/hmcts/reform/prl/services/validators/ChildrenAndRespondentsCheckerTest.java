package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Relations;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS_REVISED;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;

@RunWith(MockitoJUnitRunner.class)
public class ChildrenAndRespondentsCheckerTest {


    @Mock
    TaskErrorService taskErrorService;

    @Mock
    EventsChecker eventsChecker;

    @InjectMocks
    ChildrenAndRespondentsChecker childrenAndRespondentsChecker;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void whenNoCaseDataPresentThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build()).build();

        assertTrue(!childrenAndRespondentsChecker.isStarted(caseData));
    }

    @Test
    public void whenEmptyChildDataPresentThenIsStartedReturnsFalse() {
        ChildrenAndRespondentRelation child = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Test")
            .childFullName("Name").childAndRespondentRelation(RelationshipsEnum.other)
            .childLivesWith(YesOrNo.Yes)
            .childAndRespondentRelationOtherDetails("dsdfs")
            .build();

        Element<ChildrenAndRespondentRelation> wrappedChildren = Element.<ChildrenAndRespondentRelation>builder().value(child).build();
        List<Element<ChildrenAndRespondentRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().childAndRespondentRelations(listOfChildren).build())
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();

        assertTrue(childrenAndRespondentsChecker.isStarted(caseData));
    }


    @Test
    public void whenEmptyChildDataPresentThenIsStartedReturnsFalseWitherOther() {
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

        assertTrue(!childrenAndRespondentsChecker.isFinished(caseData));
    }

    @Test
    public void whenEmptyChildDataPresentThenIsStartedReturnsFalse1() {
        ChildrenAndRespondentRelation child = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Test")
            .childFullName("Name").childAndRespondentRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .build();

        Element<ChildrenAndRespondentRelation> wrappedChildren = Element.<ChildrenAndRespondentRelation>builder().value(child).build();
        List<Element<ChildrenAndRespondentRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
            .childAndRespondentRelations(listOfChildren).build())
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();

        assertTrue(childrenAndRespondentsChecker.isFinished(caseData));
    }


    @Test
    public void whenEmptyisFinishedThenReturnsFalse() {

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build())
            .build();

        assertTrue(!childrenAndRespondentsChecker.isFinished(caseData));
    }


    @Test
    public void whenSomeChildDataPresentThenIsStartedReturnsTrue() {

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build())
            .build();

        assertTrue(!childrenAndRespondentsChecker.isStarted(caseData));
    }



    @Test
    public void whenAllChildDataPresentThenIsFinishedReturnsTrueWithGenderOther() {
        ChildrenAndRespondentRelation child = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Test")
            .childFullName("Name").childAndRespondentRelation(RelationshipsEnum.other)
            .childAndRespondentRelationOtherDetails("dsdfs")
            .childLivesWith(YesOrNo.Yes)
            .build();

        Element<ChildrenAndRespondentRelation> wrappedChildren = Element.<ChildrenAndRespondentRelation>builder().value(child).build();
        List<Element<ChildrenAndRespondentRelation>> listOfChildren = Collections.singletonList(wrappedChildren);


        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
            .childAndRespondentRelations(listOfChildren).build())
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();
        assertTrue(childrenAndRespondentsChecker.isFinished(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenHasMandatoryCompletedReturnsTrue() {
        CaseData caseData = CaseData.builder().build();
        assertTrue(!childrenAndRespondentsChecker.hasMandatoryCompleted(caseData));
    }


    @Test
    public void testDefaultTaskStateWhenCannotStart() {
        assertEquals(TaskState.CANNOT_START_YET, childrenAndRespondentsChecker.getDefaultTaskState(CaseData.builder().build()));
    }

    @Test
    public void testDefaultTaskStateWhenChildDetailsDone() {
        CaseData caseData = CaseData.builder().build();
        when(eventsChecker.hasMandatoryCompleted(CHILD_DETAILS_REVISED, caseData)).thenReturn(true);
        assertEquals(TaskState.CANNOT_START_YET, childrenAndRespondentsChecker.getDefaultTaskState(caseData));
    }

    @Test
    public void testDefaultTaskStateWhenChildDetailsDoneIsFinished() {
        CaseData caseData = CaseData.builder().build();
        when(eventsChecker.isFinished(CHILD_DETAILS_REVISED, caseData)).thenReturn(true);
        assertEquals(TaskState.CANNOT_START_YET, childrenAndRespondentsChecker.getDefaultTaskState(caseData));
    }

    @Test
    public void testDefaultTaskStateWhenChildDetailsDoneAndApplicantDetails() {
        CaseData caseData = CaseData.builder().build();
        when(eventsChecker.hasMandatoryCompleted(CHILD_DETAILS_REVISED, caseData)).thenReturn(true);
        when(eventsChecker.hasMandatoryCompleted(RESPONDENT_DETAILS, caseData)).thenReturn(true);
        assertEquals(TaskState.NOT_STARTED, childrenAndRespondentsChecker.getDefaultTaskState(caseData));
    }
}
