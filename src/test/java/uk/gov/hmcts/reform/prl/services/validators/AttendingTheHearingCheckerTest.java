package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class AttendingTheHearingCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    AttendingTheHearingChecker attendingTheHearingChecker;

    @Test
    public void whenNoCaseDataThenIsStartedFalse() {
        CaseData caseData = CaseData.builder()
            .attendHearing(
                AttendHearing.builder().build()
            ).build();
        assertFalse(attendingTheHearingChecker.isStarted(caseData));
    }

    @Test
    public void whenPartialCaseDataThenIsStartedTrue() {
        CaseData caseData = CaseData.builder()
            .attendHearing(AttendHearing.builder()
                               .isDisabilityPresent(Yes)
                               .build())
            .build();
        assertTrue(attendingTheHearingChecker.isStarted(caseData));
    }

    @Test
    public void whenNoCaseDataThenIsFinishedFalse() {
        CaseData caseData = CaseData.builder()
            .attendHearing(AttendHearing.builder().build()).build();
        assertFalse(attendingTheHearingChecker.isFinished(caseData));
    }

    @Test
    public void whenPartialCaseDataThenIsFinishedFalse() {
        CaseData caseData = CaseData.builder()
            .attendHearing(AttendHearing.builder()
                               .isWelshNeeded(No)
                               .isDisabilityPresent(Yes)
                               .isInterpreterNeeded(Yes)
                               .build())
            .build();
        assertFalse(attendingTheHearingChecker.isFinished(caseData));
    }

    @Test
    public void whenFullCaseDataWithNoComplexTypesThenIsFinishedTrue() {
        CaseData caseData = CaseData.builder()
            .attendHearing(AttendHearing.builder()
                               .isWelshNeeded(No)
                               .isInterpreterNeeded(No)
                               .isDisabilityPresent(No)
                               .isSpecialArrangementsRequired(No)
                               .isIntermediaryNeeded(No)
                               .build())
            .build();

        assertTrue(attendingTheHearingChecker.isFinished(caseData));
    }

    @Test
    public void whenNoCaseDataHasMandatoryReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(attendingTheHearingChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenFullCaseDataHasMandatoryReturnsFalse() {
        CaseData caseData = CaseData.builder()
            .attendHearing(AttendHearing.builder()
                               .isWelshNeeded(No)
                               .isInterpreterNeeded(No)
                               .isDisabilityPresent(No)
                               .isSpecialArrangementsRequired(No)
                               .isIntermediaryNeeded(No)
                               .build())
            .build();

        assertFalse(attendingTheHearingChecker.hasMandatoryCompleted(caseData));
    }


    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(attendingTheHearingChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
