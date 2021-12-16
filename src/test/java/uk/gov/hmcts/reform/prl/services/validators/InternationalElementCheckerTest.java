package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;

@RunWith(MockitoJUnitRunner.class)
public class InternationalElementCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    InternationalElementChecker internationalElementChecker;

    @Test
    public void MandatoryAlwaysFalse(){


        CaseData caseData=CaseData.builder().build();
        boolean hasMandatory=internationalElementChecker.hasMandatoryCompleted(caseData);
        assert (!hasMandatory);
    }

    @Test
    public void NotFinishedWithEmptyFields(){

        CaseData caseData=CaseData.builder().build();
        boolean isFinished=internationalElementChecker.isFinished(caseData);
        assert (!isFinished);
    }

    @Test
    public void NotFinishedWithFieldValuesYes(){

        CaseData caseData=CaseData.builder().habitualResidentInOtherState(YES).jurisdictionIssue(YES)
            .requestToForeignAuthority(YES).build();
        boolean isFinished=internationalElementChecker.isFinished(caseData);
        assert (!isFinished);
    }

    @Test
    public void FinishedWithfieldValuesYes(){

        CaseData caseData=CaseData.builder()
            .habitualResidentInOtherState(YES)
            .habitualResidentInOtherStateGiveReason("reason")
            .jurisdictionIssue(YES)
            .jurisdictionIssueGiveReason("reason")
            .requestToForeignAuthority(YES)
            .requestToForeignAuthorityGiveReason("reason")
            .build();
        boolean isFinished=internationalElementChecker.isFinished(caseData);
        assert (isFinished);
    }

    @Test
    public void NotStartedWithBlankOrEmptyFieldValues(){

        CaseData caseData=CaseData.builder().habitualResidentInOtherState(null).jurisdictionIssue(null)
            .requestToForeignAuthority(null).build();
        boolean isStarted=internationalElementChecker.isStarted(caseData);
        assert !(isStarted);
    }

    @Test
    public void NotStartedWithUnattendedFieldValues(){
        CaseData caseData=CaseData.builder().build();
        boolean isStarted=internationalElementChecker.isStarted(caseData);
        assert (!isStarted);
    }

}
