package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_1;



@ExtendWith(MockitoExtension.class)
class RespondentBehaviourCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    RespondentBehaviourChecker respondentBehaviourChecker;

    private  CaseData caseData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        caseData = CaseData.builder().build();
    }

    @Test
    void whenNoCaseDataThenIsStartedReturnsFalse() {
        assertFalse(respondentBehaviourChecker.isStarted(caseData));
    }

    @Test
    void whenBasicRespondentBehaivourCaseDataPresentThenIsStartedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();
        caseData = caseData.toBuilder()
                       .respondentBehaviourData(respondentBehaviour).build();
        assertTrue(respondentBehaviourChecker.isStarted(caseData));
    }

    @Test
    void whenAllDetailsPresentThenIsStartedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();
        assertTrue(respondentBehaviourChecker.isStarted(caseData));
    }

    @Test
    void whenOneofRespondentBehaviourDataIsPresentIsStartedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoingToChild(Collections.singletonList(applicantStopFromRespondentDoingToChildEnum_Value_1))
            .build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();
        assertTrue(respondentBehaviourChecker.isStarted(caseData));
    }

    @Test
    void whenApplicantWantToStopFromRespondentDoingAndStopRespondingToChildIsPresentIsStartedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .applicantWantToStopFromRespondentDoingToChild(Collections.singletonList(applicantStopFromRespondentDoingToChildEnum_Value_1))
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1))
            .build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();
        assertTrue(respondentBehaviourChecker.isStarted(caseData));
    }

    @Test
    void whenApplicantWantToStopFromRespondentDoingToChildIsPresentIsStartedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .applicantWantToStopFromRespondentDoingToChild(Collections.singletonList(applicantStopFromRespondentDoingToChildEnum_Value_1))
            .build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();
        assertTrue(respondentBehaviourChecker.isStarted(caseData));
    }

    @Test
    void whenOtherReasonApplicantWantToStopFromRespondentDoingIsPresentIsStartedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();
        assertTrue(respondentBehaviourChecker.isStarted(caseData));
    }

    @Test
    void whenAllCaseDataPresentIsStartedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoingToChild(Collections.singletonList(applicantStopFromRespondentDoingToChildEnum_Value_1))
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1))
            .build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();
        assertTrue(respondentBehaviourChecker.isStarted(caseData));
    }

    @Test
    void whenNoDataIsFinishedReturnsFalse() {
        caseData = caseData.toBuilder().build();
        assertFalse(respondentBehaviourChecker.isFinished(caseData));
    }

    @Test
    void whenOneMandatoryFilledIsFinishedReturnsFalse() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data").build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertFalse(respondentBehaviourChecker.isFinished(caseData));
    }

    @Test
    void whenApplicantHasDetailsProvidedIsFinishedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertTrue(respondentBehaviourChecker.isFinished(caseData));
    }

    @Test
    void whenAllDetailsProvidedIsFinishedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoingToChild(Collections.singletonList(applicantStopFromRespondentDoingToChildEnum_Value_1))
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertTrue(respondentBehaviourChecker.isFinished(caseData));
    }

    @Test
    void whenNoDataHasMandatoryCompletedReturnsFalse() {
        caseData = caseData.toBuilder().build();
        assertFalse(respondentBehaviourChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenPartialCaseDataPresentHasMandatoryCompletedReturnsFalse() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertFalse(respondentBehaviourChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenOnlyApplicantWantToStopFromRespondentDoingDataPresentHasMandatoryCompletedReturnsFalse() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertFalse(respondentBehaviourChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenOnlyapplicantWantToStopFromRespondentDoingPresentHasMandatoryCompletedReturnsFalse() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();;
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertFalse(respondentBehaviourChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenCaseDataHasAllMandatoryFieldsHasMandatoryCompletedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();;
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertTrue(respondentBehaviourChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenAllDataHasCompletedMandatoryCompletedReturnsTrue() {
        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoingToChild(Collections.singletonList(applicantStopFromRespondentDoingToChildEnum_Value_1))
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();
        caseData = caseData.toBuilder()
            .respondentBehaviourData(respondentBehaviour).build();

        assertTrue(respondentBehaviourChecker.isFinished(caseData));
    }

    @Test
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(respondentBehaviourChecker.getDefaultTaskState(CaseData.builder().build()));
    }

}
