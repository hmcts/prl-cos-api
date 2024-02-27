package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationObjectType;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationOptionsInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import  static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class RespondentRelationshipCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @Mock
    RespondentRelationObjectType respondentRelationObjectType;
    @InjectMocks
    RespondentRelationshipChecker respondentRelationshipChecker;

    @Test
    public void whenNoCaseDataPresentThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();
        assertTrue(!respondentRelationshipChecker.isStarted(caseData));
    }

    @Test
    public void whenEmptyRelationshipDataPresentThenIsStartedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder().build())
            .build();

        assertTrue(respondentRelationshipChecker.isStarted(caseData));
    }

    @Test
    public void whenSomeRelationshipDataPresentThenIsStartedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                              .applicantRelationship(ApplicantRelationshipEnum.noneOfTheAbove)
                                              .build())
            .build();
        assertTrue(respondentRelationshipChecker.isStarted(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertTrue(!respondentRelationshipChecker.isFinished(caseData));

    }

    @Test
    public void whenSomeRealtionshipDataPresentThenIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                              .applicantRelationship(ApplicantRelationshipEnum.noneOfTheAbove)
                                              .build())
            .build();

        assertTrue(!respondentRelationshipChecker.isFinished(caseData));

    }

    @Test
    public void whenAllRelationshipDataPresentThenIsFinishedReturnsTrue() {

        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                              .applicantRelationship(ApplicantRelationshipEnum.noneOfTheAbove)
                                              .build())
            .respondentRelationOptions(RespondentRelationOptionsInfo.builder()
                                               .applicantRelationshipOptions(ApplicantRelationshipOptionsEnum.aunt)
                                               .build())
            .build();
        assertTrue(respondentRelationshipChecker.isFinished(caseData));
    }

    @Test
    public void whenRelationshipDataSelectedNotNoneOfTheAboveThenIsFinishedReturnsTrue() {

        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                              .applicantRelationship(ApplicantRelationshipEnum.formerlyEngagedOrProposed)
                                              .build())
            .build();
        assertTrue(respondentRelationshipChecker.isFinished(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(respondentRelationshipChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
