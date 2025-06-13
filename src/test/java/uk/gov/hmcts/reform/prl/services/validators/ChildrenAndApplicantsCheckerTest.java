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
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndApplicantRelation;
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
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS_REVISED;

@ExtendWith(MockitoExtension.class)
class ChildrenAndApplicantsCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @Mock
    EventsChecker eventsChecker;

    @InjectMocks
    ChildrenAndApplicantsChecker childrenAndApplicantsChecker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenNoCaseDataPresentThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build()).build();

        assertFalse(childrenAndApplicantsChecker.isStarted(caseData));
    }

    @Test
    void whenEmptyChildDataPresentThenIsStartedReturnsTrue() {
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
    void whenSomeChildDataPresentThenIsStartedReturnsTrue() {

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build())
            .build();

        assertFalse(childrenAndApplicantsChecker.isStarted(caseData));
    }



    @Test
    void whenAllChildDataPresentThenIsFinishedReturnsTrueWithGenderOther() {
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
    void whenAllChildDataPresentThenIsFinishedReturnsFalseWithGenderOther() {
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
        assertFalse(childrenAndApplicantsChecker.isFinished(caseData));
    }


    @Test
    void whenAllChildDataPresentThenIsFinishedReturnsFalse1() {
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
    void whenAllChildDataPresentThenIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build())
            .build();
        assertFalse(childrenAndApplicantsChecker.isFinished(caseData));
    }

    @Test
    void whenNoCaseDataPresentThenHasMandatoryCompletedReturnsTrue() {
        CaseData caseData = CaseData.builder().build();
        assertFalse(childrenAndApplicantsChecker.hasMandatoryCompleted(caseData));
    }


    @Test
    void testDefaultTaskStateWhenCannotStart() {
        assertEquals(TaskState.CANNOT_START_YET, childrenAndApplicantsChecker.getDefaultTaskState(CaseData.builder().build()));
    }

    @Test
    void testDefaultTaskStateWhenChildDetailsDone() {
        CaseData caseData = CaseData.builder().build();
        when(eventsChecker.hasMandatoryCompleted(CHILD_DETAILS_REVISED, caseData)).thenReturn(true);
        assertEquals(TaskState.CANNOT_START_YET, childrenAndApplicantsChecker.getDefaultTaskState(caseData));
    }

    @Test
    void testDefaultTaskStateWhenChildDetailsDoneIsFinished() {
        CaseData caseData = CaseData.builder().build();
        when(eventsChecker.isFinished(CHILD_DETAILS_REVISED, caseData)).thenReturn(true);
        assertEquals(TaskState.CANNOT_START_YET, childrenAndApplicantsChecker.getDefaultTaskState(caseData));
    }

    @Test
    void testDefaultTaskStateWhenChildDetailsDoneAndApplicantDetails() {
        CaseData caseData = CaseData.builder().build();
        when(eventsChecker.hasMandatoryCompleted(CHILD_DETAILS_REVISED, caseData)).thenReturn(true);
        when(eventsChecker.hasMandatoryCompleted(APPLICANT_DETAILS, caseData)).thenReturn(true);
        assertEquals(TaskState.NOT_STARTED, childrenAndApplicantsChecker.getDefaultTaskState(caseData));
    }
}
