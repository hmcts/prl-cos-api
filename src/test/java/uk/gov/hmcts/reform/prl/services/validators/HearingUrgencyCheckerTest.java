package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class HearingUrgencyCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    HearingUrgencyChecker hearingUrgencyChecker;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void notFinishedWhenIsCaseUrgentNotSet() {

        CaseData casedata = CaseData.builder().build();

        boolean isFinished = hearingUrgencyChecker.isFinished(casedata);

        assertFalse(isFinished);
    }

    @Test
    public void finishedWhenIsCaseUrgentSetToNo() {

        CaseData casedata = CaseData.builder().isCaseUrgent(No)
            .doYouNeedAWithoutNoticeHearing(No)
            .areRespondentsAwareOfProceedings(No)
            .doYouRequireAHearingWithReducedNotice(No)
            .build();

        boolean isFinished = hearingUrgencyChecker.isFinished(casedata);

        assertTrue(isFinished);
    }

    @Test
    public void finishedWhenIsCaseUrgentSetToYes() {

        CaseData casedata = CaseData.builder().isCaseUrgent(Yes)
            .doYouNeedAWithoutNoticeHearing(Yes)
            .caseUrgencyTimeAndReason("reason")
            .effortsMadeWithRespondents("efforts")
            .reasonsForApplicationWithoutNotice("test")
            .setOutReasonsBelow("test")
            .areRespondentsAwareOfProceedings(No)
            .doYouRequireAHearingWithReducedNotice(No)
            .build();
        boolean isFinished = hearingUrgencyChecker.isFinished(casedata);

        assertTrue(isFinished);
    }

    @Test
    public void finishedWhenIsReducedNoticeHearingSetToYes() {

        CaseData casedata = CaseData.builder().isCaseUrgent(Yes)
            .doYouNeedAWithoutNoticeHearing(Yes)
            .caseUrgencyTimeAndReason("reason")
            .effortsMadeWithRespondents("efforts")
            .reasonsForApplicationWithoutNotice("test")
            .setOutReasonsBelow("test")
            .areRespondentsAwareOfProceedings(No)
            .doYouRequireAHearingWithReducedNotice(Yes)
            .build();
        boolean isFinished = hearingUrgencyChecker.isFinished(casedata);

        assertTrue(isFinished);
    }

    @Test
    public void finishedWhenRespondentsAwareOfProceedingsSetToYes() {

        CaseData casedata = CaseData.builder().isCaseUrgent(Yes)
            .caseUrgencyTimeAndReason("reason")
            .effortsMadeWithRespondents("efforts")
            .doYouNeedAWithoutNoticeHearing(Yes)
            .reasonsForApplicationWithoutNotice("test")
            .setOutReasonsBelow("test")
            .areRespondentsAwareOfProceedings(Yes)
            .doYouRequireAHearingWithReducedNotice(Yes)
            .build();
        boolean isFinished = hearingUrgencyChecker.isFinished(casedata);

        assertTrue(isFinished);
    }

    @Test
    public void startedWhenNonEmptyCaseData() {

        CaseData casedata = CaseData.builder()
            .caseUrgencyTimeAndReason("reason")
            .build();

        boolean isStarted = hearingUrgencyChecker.isStarted(casedata);

        assertTrue(isStarted);
    }

    @Test
    public void notStartedWhenEmptyCaseData() {

        CaseData casedata = CaseData.builder()
            .caseUrgencyTimeAndReason("reason")
            .build();

        boolean isStarted = hearingUrgencyChecker.isStarted(casedata);

        assertTrue(isStarted);
    }

    @Test
    public void mandatoryNotCompletedWhenCaseDataEmpty() {

        CaseData casedata = CaseData.builder().build();

        boolean isMandatory = hearingUrgencyChecker.hasMandatoryCompleted(casedata);

        assertFalse(isMandatory);
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(hearingUrgencyChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
