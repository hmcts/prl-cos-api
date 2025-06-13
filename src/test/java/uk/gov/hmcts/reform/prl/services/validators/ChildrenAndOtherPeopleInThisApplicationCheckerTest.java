package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Relations;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS_REVISED;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE_REVISED;

@ExtendWith(MockitoExtension.class)
class ChildrenAndOtherPeopleInThisApplicationCheckerTest {

    @InjectMocks
    ChildrenAndOtherPeopleInThisApplicationChecker childrenAndOtherPeopleInThisApplicationChecker;

    @Mock
    TaskErrorService taskErrorService;

    @Mock
    EventsChecker eventsChecker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsStartedChildAndOtherPeoplesRelationsPresent() {
        ChildrenAndOtherPeopleRelation child = ChildrenAndOtherPeopleRelation.builder()
                .build();

        Element<ChildrenAndOtherPeopleRelation> wrappedChildren = Element.<ChildrenAndOtherPeopleRelation>builder().value(child).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
                        .childAndOtherPeopleRelations(listOfChildren).build())
                .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
                .build();

        assertTrue(childrenAndOtherPeopleInThisApplicationChecker.isStarted(caseData));
    }

    @Test
    void testIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build())
                .build();

        assertFalse(childrenAndOtherPeopleInThisApplicationChecker.isStarted(caseData));
    }

    @Test
    void testIsFinishedReturnsTrueWhenAllDataThere() {
        ChildrenAndOtherPeopleRelation child = ChildrenAndOtherPeopleRelation.builder()
                .childLivesWith(YesOrNo.Yes)
                .otherPeopleFullName("test")
                .childAndOtherPeopleRelation(RelationshipsEnum.other)
                .childFullName("test")
                .isChildLivesWithPersonConfidential(YesOrNo.No)
                .childAndOtherPeopleRelationOtherDetails("test")
                .build();

        Element<ChildrenAndOtherPeopleRelation> wrappedChildren = Element.<ChildrenAndOtherPeopleRelation>builder().value(child).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
                        .childAndOtherPeopleRelations(listOfChildren).build())
                .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
                .build();
        assertTrue(childrenAndOtherPeopleInThisApplicationChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedNotAllInformationPresent() {
        ChildrenAndOtherPeopleRelation child = ChildrenAndOtherPeopleRelation.builder()
                .childFullName("Name").childAndOtherPeopleRelation(RelationshipsEnum.father)
                .childLivesWith(YesOrNo.No)
                .build();

        Element<ChildrenAndOtherPeopleRelation> wrappedChildren = Element.<ChildrenAndOtherPeopleRelation>builder().value(child).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
                        .childAndOtherPeopleRelations(listOfChildren).build()).childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
                .build();
        assertFalse(childrenAndOtherPeopleInThisApplicationChecker.isFinished(caseData));
    }

    @Test
    void whenAllChildDataPresentThenIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build())
                .build();
        assertFalse(childrenAndOtherPeopleInThisApplicationChecker.isFinished(caseData));
    }

    @Test
    void whenAllChildDataPresentThenIsFinishedReturnsFalseWithErrors() {
        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build())
                .build();

        when(eventsChecker.hasMandatoryCompleted(CHILD_DETAILS_REVISED, caseData)).thenReturn(true);
        when(eventsChecker.hasMandatoryCompleted(OTHER_PEOPLE_IN_THE_CASE_REVISED, caseData)).thenReturn(true);
        assertFalse(childrenAndOtherPeopleInThisApplicationChecker.isFinished(caseData));
    }

    @Test
    void whenAllChildDataPresentAndIsTaskCanBeEnabledFalseThenIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().childAndOtherPeopleRelations(null).build())
                .build();

        assertFalse(childrenAndOtherPeopleInThisApplicationChecker.isFinished(caseData));
    }

    @Test
    void whenAllChildDataPresentAndChildAndOtherPeopleRekationsIsNullThenIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().childAndOtherPeopleRelations(null).build())
                .build();

        assertFalse(childrenAndOtherPeopleInThisApplicationChecker.isFinished(caseData));
    }

    @Test
    void whenNoCaseDataPresentThenHasMandatoryCompletedReturnsTrue() {
        CaseData caseData = CaseData.builder().build();
        assertFalse(childrenAndOtherPeopleInThisApplicationChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void testDefaultTaskStateWhenCannotStart() {
        assertEquals(TaskState.CANNOT_START_YET, childrenAndOtherPeopleInThisApplicationChecker.getDefaultTaskState(CaseData.builder().build()));
    }

    @Test
    void testDefaultTaskStateWhenChildDetailsDone() {
        CaseData caseData = CaseData.builder().build();
        when(eventsChecker.hasMandatoryCompleted(CHILD_DETAILS_REVISED, caseData)).thenReturn(true);
        assertEquals(TaskState.CANNOT_START_YET, childrenAndOtherPeopleInThisApplicationChecker.getDefaultTaskState(caseData));
    }

    @Test
    void testDefaultTaskStateWhenChildDetailsDoneIsFinished() {
        CaseData caseData = CaseData.builder().build();
        when(eventsChecker.isFinished(CHILD_DETAILS_REVISED, caseData)).thenReturn(true);
        assertEquals(TaskState.CANNOT_START_YET, childrenAndOtherPeopleInThisApplicationChecker.getDefaultTaskState(caseData));
    }

    @Test
    void testDefaultTaskStateWhenChildDetailsDoneAndOtherPeopleDetails() {
        CaseData caseData = CaseData.builder().build();
        when(eventsChecker.hasMandatoryCompleted(CHILD_DETAILS_REVISED, caseData)).thenReturn(true);
        when(eventsChecker.hasMandatoryCompleted(OTHER_PEOPLE_IN_THE_CASE_REVISED, caseData)).thenReturn(true);
        assertEquals(TaskState.NOT_STARTED, childrenAndOtherPeopleInThisApplicationChecker.getDefaultTaskState(caseData));
    }

    @Test
    void testDefaultTaskStateWhenChildDetailsDoneAndOtherDetailsDetailsIsFinished() {
        CaseData caseData = CaseData.builder().build();
        when(eventsChecker.hasMandatoryCompleted(CHILD_DETAILS_REVISED, caseData)).thenReturn(true);
        when(eventsChecker.isFinished(OTHER_PEOPLE_IN_THE_CASE_REVISED, caseData)).thenReturn(true);
        assertEquals(TaskState.NOT_STARTED, childrenAndOtherPeopleInThisApplicationChecker.getDefaultTaskState(caseData));
    }
}
