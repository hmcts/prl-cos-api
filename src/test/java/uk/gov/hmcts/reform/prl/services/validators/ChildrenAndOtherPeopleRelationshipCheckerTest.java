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
import uk.gov.hmcts.reform.prl.services.TaskErrorService;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ChildrenAndOtherPeopleRelationshipCheckerTest {
    @Mock
    TaskErrorService taskErrorService;

    @Mock
    EventsChecker eventsChecker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @InjectMocks
    ChildrenAndOtherPeopleInThisApplicationChecker otherPeopleInTheCaseChecker;

    @Test
    void whenNoCaseDataPresentThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build()).build();

        assertFalse(otherPeopleInTheCaseChecker.isStarted(caseData));
    }

    @Test
    void whenEmptyChildDataPresentThenIsStartedReturnsFalse() {
        ChildrenAndOtherPeopleRelation child = ChildrenAndOtherPeopleRelation.builder().build();
        Element<ChildrenAndOtherPeopleRelation> wrappedChildren =
            Element.<ChildrenAndOtherPeopleRelation>builder().value(child).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().relations(Relations.builder()
                .childAndOtherPeopleRelations(listOfChildren).build()).build();

        assertTrue(otherPeopleInTheCaseChecker.isStarted(caseData));
    }

    @Test
    void whenSomeChildDataPresentThenIsStartedReturnsTrue() {

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build())
            .build();

        assertFalse(otherPeopleInTheCaseChecker.isStarted(caseData));
    }


    @Test
    void whenSomeChildDataPresentThenIsFinishedReturnsFalse() {
        ChildrenAndOtherPeopleRelation child = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test").childFullName("Name")
            .childAndOtherPeopleRelation(RelationshipsEnum.father).build();
        Element<ChildrenAndOtherPeopleRelation> wrappedChildren = Element.<ChildrenAndOtherPeopleRelation>builder().value(child).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().relations(Relations.builder().childAndOtherPeopleRelations(listOfChildren).build()).build();

        assertFalse(otherPeopleInTheCaseChecker.isFinished(caseData));
    }

    @Test
    void whenAllChildDataPresentThenIsFinishedReturnsTrue() {
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
                .relations(Relations.builder().childAndOtherPeopleRelations(listOfChildren).build())
            .build();

        assertTrue(otherPeopleInTheCaseChecker.isFinished(caseData));
    }

    @Test
    void whenValidateOtherChildrenNotInTheCaseReturnsTrue() {
        ChildrenAndOtherPeopleRelation child = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test")
            .childFullName("Name").childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();

        Element<ChildrenAndOtherPeopleRelation> wrappedChildren = Element.<ChildrenAndOtherPeopleRelation>builder().value(child).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
            .childAndOtherPeopleRelations(listOfChildren).build())
            .build();

        assertTrue(otherPeopleInTheCaseChecker.isFinished(caseData));
    }


    @Test
    void whenValidateOtherChildrenNotInTheCaseReturnsFalse() {
        CaseData caseData = CaseData.builder()
                .relations(Relations.builder().build())
            .build();
        assertFalse(otherPeopleInTheCaseChecker.isFinished(caseData));
    }


    @Test
    void whenAllChildDataPresentThenIsFinishedReturnsNull() {
        ChildrenAndOtherPeopleRelation child = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test")
            .childFullName("Name").childAndOtherPeopleRelation(null)
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();

        Element<ChildrenAndOtherPeopleRelation> wrappedChildren = Element.<ChildrenAndOtherPeopleRelation>builder().value(child).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
            .childAndOtherPeopleRelations(listOfChildren)
            .build()).childrenNotPartInTheCaseYesNo(YesOrNo.Yes).build();
        assertTrue(otherPeopleInTheCaseChecker.isFinished(caseData));
    }



    @Test
    void whenAllChildDataPresentThenIsFinishedReturnsTrueWithGenderOther() {
        ChildrenAndOtherPeopleRelation child = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test")
            .childFullName("Name").childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();

        Element<ChildrenAndOtherPeopleRelation> wrappedChildren = Element.<ChildrenAndOtherPeopleRelation>builder().value(child).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
                .relations(Relations.builder()
                        .childAndOtherPeopleRelations(listOfChildren).build())
            .childrenNotPartInTheCaseYesNo(YesOrNo.Yes)
            .build();
        assertTrue(otherPeopleInTheCaseChecker.isFinished(caseData));
    }

    @Test
    void whenNoCaseDataPresentThenHasMandatoryCompletedReturnsTrue() {
        CaseData caseData = CaseData.builder().build();
        assertFalse(otherPeopleInTheCaseChecker.hasMandatoryCompleted(caseData));
    }


    @Test
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(otherPeopleInTheCaseChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
