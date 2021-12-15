package uk.gov.hmcts.reform.prl.services.validators;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;

@RunWith(MockitoJUnitRunner.class)
public class AttendingTheHearingCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    AttendingTheHearingChecker attendingTheHearingChecker;

    @Test
    public void whenNoCaseDataThenIsStartedFalse() {
        CaseData caseData = CaseData.builder().build();
        assert !attendingTheHearingChecker.isStarted(caseData);

    }

    @Test
    public void whenPartialCaseDataThenIsStartedTrue() {
        CaseData caseData = CaseData.builder().isDisabilityPresent(YES).build();
        assert attendingTheHearingChecker.isStarted(caseData);

    }

    @Test
    public void whenNoCaseDataThenIsFinishedFalse() {
        CaseData caseData = CaseData.builder().build();
        assert !attendingTheHearingChecker.isFinished(caseData);

    }

    @Test
    public void whenPartialCaseDataThenIsFinishedFalse() {
        CaseData caseData = CaseData.builder()
            .isWelshNeeded(NO)
            .isDisabilityPresent(YES)
            .isInterpreterNeeded(YES)
            .build();
        assert!attendingTheHearingChecker.isFinished(caseData);

    }

    @Test
    public void whenFullCaseDataWithNoComplexTypesThenIsFinishedTrue() {
        CaseData caseData = CaseData.builder()
            .isWelshNeeded(NO)
            .isInterpreterNeeded(NO)
            .isDisabilityPresent(NO)
            .isSpecialArrangementsRequired(NO)
            .isIntermediaryNeeded(NO)
            .build();

        assert attendingTheHearingChecker.isFinished(caseData);

    }

    @Test
    public void whenNoCaseDataHasMandatoryReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assert !attendingTheHearingChecker.hasMandatoryCompleted(caseData);
    }

    @Test
    public void whenFullCaseDataHasMandatoryReturnsFalse() {
        CaseData caseData = CaseData.builder()
            .isWelshNeeded(NO)
            .isInterpreterNeeded(NO)
            .isDisabilityPresent(NO)
            .isSpecialArrangementsRequired(NO)
            .isIntermediaryNeeded(NO)
            .build();

        assert !attendingTheHearingChecker.hasMandatoryCompleted(caseData);
    }






}
