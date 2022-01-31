package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_1;



@RunWith(MockitoJUnitRunner.class)
public class RespondentBehaviourCheckerTest {

    @InjectMocks
    RespondentBehaviourChecker respondentBehaviourChecker;

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();
        assert !respondentBehaviourChecker.isStarted(caseData);
    }

    @Test
    public void whenBasicRespondentBehaivourCaseDataPresentThenIsStartedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();
        CaseData caseData = CaseData.builder()
                       .respondentBehaviourData(respondentBehaviour).build();
        assert respondentBehaviourChecker.isStarted(caseData);
    }

    @Test
    public void whenNoDataHasMandatoryCompletedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();
        assert !respondentBehaviourChecker.hasMandatoryCompleted(caseData);
    }

    @Test
    public void whenNoDataIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();
        assert !respondentBehaviourChecker.isFinished(caseData);
    }

    @Test
    public void whenApplicantHasDetailsProvidedIsFinishedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();
        CaseData caseData = CaseData.builder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertTrue(respondentBehaviourChecker.isFinished(caseData));
    }

    @Test
    public void whenApplicantHasNotAttendedMiamButHasCompletedExemptionsSectionIsFinishedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoingToChild(Collections.singletonList(applicantStopFromRespondentDoingToChildEnum_Value_1))
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();
        CaseData caseData = CaseData.builder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertTrue(respondentBehaviourChecker.isFinished(caseData));

    }

}
