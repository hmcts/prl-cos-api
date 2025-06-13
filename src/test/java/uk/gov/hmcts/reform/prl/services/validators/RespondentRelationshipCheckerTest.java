package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationObjectType;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationOptionsInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class RespondentRelationshipCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @Mock
    RespondentRelationObjectType respondentRelationObjectType;
    @InjectMocks
    RespondentRelationshipChecker respondentRelationshipChecker;

    @Test
    void whenNoCaseDataPresentThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();
        assertFalse(respondentRelationshipChecker.isStarted(caseData));
    }

    @Test
    void whenEmptyRelationshipDataPresentThenIsStartedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder().build())
            .build();

        assertTrue(respondentRelationshipChecker.isStarted(caseData));
    }

    @Test
    void whenSomeRelationshipDataPresentThenIsStartedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                              .applicantRelationship(ApplicantRelationshipEnum.noneOfTheAbove)
                                              .build())
            .build();
        assertTrue(respondentRelationshipChecker.isStarted(caseData));
    }

    @Test
    void whenNoCaseDataPresentThenIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(respondentRelationshipChecker.isFinished(caseData));

    }

    @Test
    void whenSomeRealtionshipDataPresentThenIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                              .applicantRelationship(ApplicantRelationshipEnum.noneOfTheAbove)
                                              .build())
            .build();

        assertFalse(respondentRelationshipChecker.isFinished(caseData));

    }

    @Test
    void whenAllRelationshipDataPresentThenIsFinishedReturnsTrue() {

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
    void whenRelationshipDataSelectedNotNoneOfTheAboveThenIsFinishedReturnsTrue() {

        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                              .applicantRelationship(ApplicantRelationshipEnum.formerlyEngagedOrProposed)
                                              .build())
            .build();
        assertTrue(respondentRelationshipChecker.isFinished(caseData));
    }

    @Test
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(respondentRelationshipChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
