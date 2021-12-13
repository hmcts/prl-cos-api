package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;

public class HearingUrgencyCheckerTest {
    @Test
    public void NotFinishedWhenIsCaseUrgentNotSet(){

        CaseData casedata = CaseData.builder().build();

        HearingUrgencyChecker hearingUrgencyChecker = new HearingUrgencyChecker();

        boolean isFinished = hearingUrgencyChecker.isFinished(casedata);

        assert(!isFinished);
    }
    @Test
    public void FinishedWhenIsCaseUrgentSetToNo(){

        CaseData casedata = CaseData.builder().isCaseUrgent(NO)
            .doYouNeedAWithoutNoticeHearing(YES)
            .areRespondentsAwareOfProceedings(NO)
            .doYouRequireAHearingWithReducedNotice(YES)
            .build();

        HearingUrgencyChecker hearingUrgencyChecker = new HearingUrgencyChecker();

        boolean isFinished = hearingUrgencyChecker.isFinished(casedata);

        assert(isFinished);
    }
    @Test
    public void FinishedWhenIsCaseUrgentSetToYes(){

        CaseData casedata = CaseData.builder().isCaseUrgent(YES)
            .doYouNeedAWithoutNoticeHearing(YES)
            .caseUrgencyTimeAndReason("reason")
            .effortsMadeWithRespondents("efforts")
            .reasonsForApplicationWithoutNotice("test")
            .setOutReasonsBelow("test")
            .areRespondentsAwareOfProceedings(NO)
            .doYouRequireAHearingWithReducedNotice(NO)
            .build();

        HearingUrgencyChecker hearingUrgencyChecker = new HearingUrgencyChecker();

        boolean isFinished =hearingUrgencyChecker.isFinished(casedata);

        assert(isFinished);
    }

    @Test
    public void StartedWhenNonEmptyCaseData(){

        CaseData casedata = CaseData.builder()
            .caseUrgencyTimeAndReason("reason")
            .build();

        HearingUrgencyChecker hearingUrgencyChecker = new HearingUrgencyChecker();

        boolean isStarted =hearingUrgencyChecker.isStarted(casedata);

        assert(isStarted);
    }
    @Test
    public void NotStartedWhenEmptyCaseData(){

        CaseData casedata = CaseData.builder()
            .caseUrgencyTimeAndReason("reason")
            .build();

        HearingUrgencyChecker hearingUrgencyChecker = new HearingUrgencyChecker();

        boolean isStarted =hearingUrgencyChecker.isStarted(casedata);

        assert(isStarted);
    }

    @Test
    public void MandatoryNotCompletedWhenCaseDataEmpty(){

        CaseData casedata = CaseData.builder().build();

        HearingUrgencyChecker hearingUrgencyChecker = new HearingUrgencyChecker();

        boolean isMandatory =hearingUrgencyChecker.hasMandatoryCompleted(casedata);

        assert(!isMandatory);
    }

    @Test
    public void MandatoryCompletedIfIsCaseUrgentSetToNo(){

        CaseData casedata = CaseData.builder().isCaseUrgent(NO).build();

        HearingUrgencyChecker hearingUrgencyChecker = new HearingUrgencyChecker();

        boolean isMandatory =hearingUrgencyChecker.hasMandatoryCompleted(casedata);

        assert(isMandatory);
    }

}
