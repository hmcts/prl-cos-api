package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_1;



@RunWith(MockitoJUnitRunner.class)
public class RespondentBehaviourCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    RespondentBehaviourChecker respondentBehaviourChecker;

    private  CaseData caseData;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        caseData = CaseData.builder().build();
    }

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {
        assertFalse(respondentBehaviourChecker.isStarted(caseData));
    }

    @Test
    public void whenPartialDetailsPresentThenIsStartedReturnsFalse() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoingToChild(Collections.singletonList(applicantStopFromRespondentDoingToChildEnum_Value_1))
            .build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();
        assertTrue(respondentBehaviourChecker.isStarted(caseData));
    }

    @Test
    public void whenBasicRespondentBehaivourCaseDataPresentThenIsStartedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1))
            .build();
        caseData = caseData.toBuilder()
                       .respondentBehaviourData(respondentBehaviour).build();
        assertTrue(respondentBehaviourChecker.isStarted(caseData));
    }

    @Test
    public void whenAllDetailsPresentThenIsStartedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1))
            .applicantWantToStopFromRespondentDoingToChild(Collections.singletonList(applicantStopFromRespondentDoingToChildEnum_Value_1))
            .build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();
        assertTrue(respondentBehaviourChecker.isStarted(caseData));
    }

    @Test
    public void whenNoDataIsFinishedReturnsFalse() {
        caseData = caseData.toBuilder().build();
        assertFalse(respondentBehaviourChecker.isFinished(caseData));
    }

    @Test
    public void whenOneMandatoryFilledIsFinishedReturnsFalse() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data").build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertFalse(respondentBehaviourChecker.isFinished(caseData));
    }

    @Test
    public void whenApplicantHasDetailsProvidedIsFinishedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertTrue(respondentBehaviourChecker.isFinished(caseData));
    }

    @Test
    public void whenPartialDetailsProvidedIsFinishedReturnsFalse() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .applicantWantToStopFromRespondentDoingToChild(Collections.singletonList(applicantStopFromRespondentDoingToChildEnum_Value_1))
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertFalse(respondentBehaviourChecker.isFinished(caseData));
    }

    @Test
    public void whenAllDataHasCompletedMandatoryCompletedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoingToChild(Collections.singletonList(applicantStopFromRespondentDoingToChildEnum_Value_1))
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertTrue(respondentBehaviourChecker.isFinished(caseData));
    }

    @Test
    public void whenNoDataHasMandatoryCompletedReturnsFalse() {
        caseData = caseData.toBuilder().build();
        assertFalse(respondentBehaviourChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenDataHasMandatoryCompletedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();;
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertTrue(respondentBehaviourChecker.hasMandatoryCompleted(caseData));
    }




}
