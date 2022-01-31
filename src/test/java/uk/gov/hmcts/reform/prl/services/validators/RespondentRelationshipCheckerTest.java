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

import static org.mockito.Mockito.when;

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

        assert !respondentRelationshipChecker.isStarted(caseData);

    }

    @Test
    public void whenEmptyRelationshipDataPresentThenIsStartedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder().build())
            .build();

        assert respondentRelationshipChecker.isStarted(caseData);
    }

    @Test
    public void whenSomeRelationshipDataPresentThenIsStartedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                              .applicantRelationshipEnum(ApplicantRelationshipEnum.noneOfTheAbove)
                                              .build())
            .build();
        assert respondentRelationshipChecker.isStarted(caseData);
    }

    @Test
    public void whenNoCaseDataPresentThenIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assert !respondentRelationshipChecker.isFinished(caseData);

    }

    @Test
    public void whenSomeRealtionshipDataPresentThenIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                              .applicantRelationshipEnum(ApplicantRelationshipEnum.noneOfTheAbove)
                                              .build())
            .build();

        assert !respondentRelationshipChecker.isFinished(caseData);

    }

    @Test
    public void whenAllRelationshipDataPresentThenIsFinishedReturnsTrue() {

        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                              .applicantRelationshipEnum(ApplicantRelationshipEnum.noneOfTheAbove)
                                              .build())
            .respondentRelationOptions(RespondentRelationOptionsInfo.builder()
                                               .applicantRelationshipOptionsEnum(ApplicantRelationshipOptionsEnum.aunt)
                                               .build())
            .build();
        assert respondentRelationshipChecker.isFinished(caseData);
    }

    @Test
    public void whenRelationshipDataSelectedNotNoneOfTheAboveThenIsFinishedReturnsTrue() {

        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                              .applicantRelationshipEnum(ApplicantRelationshipEnum.formerlyEngagedOrProposed)
                                              .build())
            .build();
        assert respondentRelationshipChecker.isFinished(caseData);
    }

}
