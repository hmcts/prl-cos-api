package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

public class LitigationCapacityCheckerTest {

    LitigationCapacityChecker litigationCapacityChecker =new LitigationCapacityChecker();

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
            .litigationCapacityOtherFactors(YesOrNo.YES)
            .litigationCapacityFactors("")
            .litigationCapacityReferrals("")
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
