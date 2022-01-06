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
public class HearingUrgencyCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    HearingUrgencyChecker hearingUrgencyChecker;

    @Test
    public void notFinishedWhenIsCaseUrgentNotSet() {

        CaseData casedata = CaseData.builder().build();

        boolean isFinished = hearingUrgencyChecker.isFinished(casedata);

        assert (!isFinished);
    }

    @Test
    public void finishedWhenIsCaseUrgentSetToNo() {

        CaseData casedata = CaseData.builder().isCaseUrgent(NO)
            .doYouNeedAWithoutNoticeHearing(NO)
            .areRespondentsAwareOfProceedings(NO)
            .doYouRequireAHearingWithReducedNotice(NO)
            .build();

        boolean isFinished = hearingUrgencyChecker.isFinished(casedata);

        assert (isFinished);
    }

    @Test
    public void finishedWhenIsCaseUrgentSetToYes() {

        CaseData casedata = CaseData.builder().isCaseUrgent(YES)
            .doYouNeedAWithoutNoticeHearing(YES)
            .caseUrgencyTimeAndReason("reason")
            .effortsMadeWithRespondents("efforts")
            .reasonsForApplicationWithoutNotice("test")
            .setOutReasonsBelow("test")
            .areRespondentsAwareOfProceedings(NO)
            .doYouRequireAHearingWithReducedNotice(NO)
            .build();


        boolean isFinished = hearingUrgencyChecker.isFinished(casedata);

        assert (isFinished);
    }

    @Test
    public void startedWhenNonEmptyCaseData() {

        CaseData casedata = CaseData.builder()
            .caseUrgencyTimeAndReason("reason")
            .build();

        boolean isStarted = hearingUrgencyChecker.isStarted(casedata);

        assert (isStarted);
    }

    @Test
    public void notStartedWhenEmptyCaseData() {

        CaseData casedata = CaseData.builder()
            .caseUrgencyTimeAndReason("reason")
            .build();

        boolean isStarted = hearingUrgencyChecker.isStarted(casedata);

        assert (isStarted);
    }

    @Test
    public void mandatoryNotCompletedWhenCaseDataEmpty() {

        CaseData casedata = CaseData.builder().build();

        boolean isMandatory = hearingUrgencyChecker.hasMandatoryCompleted(casedata);

        assert (!isMandatory);
    }

    @Test
    public void mandatoryCompletedIfIsCaseUrgentSetToNo() {

        CaseData casedata = CaseData.builder().isCaseUrgent(NO).build();

        boolean isMandatory = hearingUrgencyChecker.hasMandatoryCompleted(casedata);

        assert (isMandatory);
    }

}
