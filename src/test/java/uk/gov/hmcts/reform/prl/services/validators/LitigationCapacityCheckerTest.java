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
    public void mandatoryAlwaysFalse() {

        CaseData caseData = CaseData.builder().build();
        boolean hasMandatory = litigationCapacityChecker.hasMandatoryCompleted(caseData);
        assert (!hasMandatory);
    }

    @Test
    public void notStartedWithoutFieldValues() {
        CaseData caseData = CaseData.builder().build();
        boolean isStarted = litigationCapacityChecker.isStarted(caseData);
        assert (!isStarted);
    }

    @Test
    public void startedWithFieldOtherFactors() {
        CaseData caseData = CaseData.builder().litigationCapacityOtherFactors(YesOrNo.Yes).build();
        boolean isStarted = litigationCapacityChecker.isStarted(caseData);
        assert (isStarted);
    }

    @Test
    public void notFinishedWithNullLitigationValues() {
        CaseData caseData = CaseData.builder().build();
        boolean isFinished = litigationCapacityChecker.isFinished(caseData);
        assert (!isFinished);
    }

    @Test
    public void finishedWithLitigationOtherFactors() {
        CaseData caseData = CaseData.builder()
            .litigationCapacityOtherFactors(YesOrNo.Yes)
            .litigationCapacityOtherFactorsDetails("")
            .build();
        boolean isFinished = litigationCapacityChecker.isFinished(caseData);
        assert (isFinished);
    }

    @Test
    public void finishedWithLitigationFactorsOrReferrals() {
        CaseData caseData = CaseData.builder()
            .litigationCapacityOtherFactors(YesOrNo.No)
            .litigationCapacityFactors("Test")
            .litigationCapacityReferrals("test ")
            .build();
        boolean isFinished = litigationCapacityChecker.isFinished(caseData);
        assert (isFinished);
    }

    @Test
    public void finishedWithOtherFactorsAsNo() {
        CaseData caseData = CaseData.builder()
            .litigationCapacityOtherFactors(YesOrNo.No)
            .build();
        boolean isFinished = litigationCapacityChecker.isFinished(caseData);
        assert (isFinished);
    }

}
