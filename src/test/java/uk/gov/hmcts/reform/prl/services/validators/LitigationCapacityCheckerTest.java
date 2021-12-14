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
public class LitigationCapacityCheckerTest {


    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    LitigationCapacityChecker litigationCapacityChecker;

    @Test
    public void MandatoryAlwaysFalse(){


        CaseData caseData=CaseData.builder().build();
        boolean hasMandatory=litigationCapacityChecker.hasMandatoryCompleted(caseData);
        assert (!hasMandatory);
    }

    @Test
    public void NotStartedWithoutFieldValues(){
        CaseData caseData=CaseData.builder().build();
        boolean isStarted = litigationCapacityChecker.isStarted(caseData);
        assert (!isStarted);
    }

    @Test
    public void StartedWithFieldOtherFactors(){
        CaseData caseData=CaseData.builder().litigationCapacityOtherFactors(YesOrNo.YES).build();
        boolean isStarted = litigationCapacityChecker.isStarted(caseData);
        assert (isStarted);
    }

    @Test
    public void NotFinishedWithNullLitigationValues(){
        CaseData caseData=CaseData.builder().build();
        boolean isFinished = litigationCapacityChecker.isFinished(caseData);
        assert (!isFinished);
    }

    @Test
    public void FinishedWithLitigationOtherFactors(){
        CaseData caseData=CaseData.builder()
            .litigationCapacityOtherFactors(YesOrNo.YES)
            .litigationCapacityOtherFactorsDetails("")
            .build();
        boolean isFinished = litigationCapacityChecker.isFinished(caseData);
        assert (isFinished);
    }

    @Test
    public void FinishedWithLitigationFactorsOrReferrals(){
        CaseData caseData=CaseData.builder()
            .litigationCapacityOtherFactors(YesOrNo.NO)
            .litigationCapacityFactors("Test")
            .litigationCapacityReferrals("test ")
            .build();
        boolean isFinished = litigationCapacityChecker.isFinished(caseData);
        assert (isFinished);
    }

    @Test
    public void FinishedWithOtherFactorsAsNo(){
        CaseData caseData=CaseData.builder()
            .litigationCapacityOtherFactors(YesOrNo.NO)
            .build();
        boolean isFinished = litigationCapacityChecker.isFinished(caseData);
        assert (isFinished);
    }

}
