package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class AttendingTheHearingCheckerTest {

    @InjectMocks
    AttendingTheHearingChecker attendingTheHearingChecker;

    @Mock
    TaskErrorService taskErrorService;

    @Test
    void whenNoCaseDataThenIsStartedFalse() {
        CaseData caseData = CaseData.builder()
            .attendHearing(
                AttendHearing.builder().build()
            ).build();
        assertFalse(attendingTheHearingChecker.isStarted(caseData));
    }

    @Test
    void whenPartialCaseDataThenIsStartedTrue() {
        CaseData caseData = CaseData.builder()
            .attendHearing(AttendHearing.builder()
                               .isDisabilityPresent(Yes)
                               .build())
            .build();
        assertTrue(attendingTheHearingChecker.isStarted(caseData));
    }

    @Test
    void whenNoCaseDataThenIsFinishedFalse() {
        CaseData caseData = CaseData.builder()
            .attendHearing(AttendHearing.builder().build()).build();
        assertFalse(attendingTheHearingChecker.isFinished(caseData));
    }

    @Test
    void whenPartialCaseDataThenIsFinishedFalse() {
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
    void whenFullCaseDataWithNoComplexTypesThenIsFinishedTrue() {
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
    void whenNoCaseDataHasMandatoryReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(attendingTheHearingChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenFullCaseDataHasMandatoryReturnsFalse() {
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
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(attendingTheHearingChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
