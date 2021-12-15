package uk.gov.hmcts.reform.prl.services.validators;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

@RunWith(MockitoJUnitRunner.class)
public class AllegationsOfHarmCheckerTest {
    @Mock
    TaskErrorService taskErrorService;
    @InjectMocks
    AllegationsOfHarmChecker allegationsOfHarmChecker;

    @Test
    public  void  NotFinishedFieldsNotValidatedToTrue(){
        CaseData casedata = CaseData.builder().build();

        boolean isFinished = allegationsOfHarmChecker.isFinished(casedata);

        assert(!isFinished);
    }

    @Test
    public  void  FinishedFieldsValidatedToTrue(){
        CaseData casedata = CaseData.builder()
            .allegationsOfHarmYesNo(YesOrNo.NO)
            .build();

        boolean isFinished = allegationsOfHarmChecker.isFinished(casedata);

        assert(isFinished);
    }

    @Test
    public void ValidateAbusePresentFalse(){
        CaseData casedata = CaseData.builder().build();

        boolean isAbusePresent = allegationsOfHarmChecker.isStarted(casedata);

        assert(!isAbusePresent);
    }

    @Test
    public  void  FinishedWhenAbusePresent(){
        CaseData casedata = CaseData.builder()
            .allegationsOfHarmYesNo(YesOrNo.YES)
            .allegationsOfHarmChildAbductionYesNo(YesOrNo.YES)
            .allegationsOfHarmDomesticAbuseYesNo(YesOrNo.NO)
            .abductionCourtStepsRequested("")
            .childAbductionReasons("reason")
            .previousAbductionThreats(YesOrNo.NO)
            .abductionPreviousPoliceInvolvement(YesOrNo.NO)
            .abductionOtherSafetyConcerns(YesOrNo.NO)
            .ordersNonMolestation(YesOrNo.YES)
            .ordersOccupation(YesOrNo.NO)
            .ordersForcedMarriageProtection(YesOrNo.YES)
            .ordersRestraining(YesOrNo.NO)
            .ordersOtherInjunctive(YesOrNo.NO)
            .ordersUndertakingInPlace(YesOrNo.YES)
            .allegationsOfHarmOtherConcerns(YesOrNo.NO)
            .allegationsOfHarmOtherConcernsCourtActions("action")
            .build();

        boolean isFinished = allegationsOfHarmChecker.isFinished(casedata);

        assert(isFinished);
    }
}
