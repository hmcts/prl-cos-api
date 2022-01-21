package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.yes;

@RunWith(MockitoJUnitRunner.class)
public class InternationalElementCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    InternationalElementChecker internationalElementChecker;

    @Test
    public void mandatoryAlwaysFalse() {


        CaseData caseData = CaseData.builder().build();
        boolean hasMandatory = internationalElementChecker.hasMandatoryCompleted(caseData);
        assert (!hasMandatory);
    }

    @Test
    public void notFinishedWithEmptyFields() {

        CaseData caseData = CaseData.builder().build();
        boolean isFinished = internationalElementChecker.isFinished(caseData);
        assert (!isFinished);
    }

    @Test
    public void notFinishedWithFieldValuesYes() {

        CaseData caseData = CaseData.builder().habitualResidentInOtherState(yes).jurisdictionIssue(yes)
            .requestToForeignAuthority(yes).build();
        boolean isFinished = internationalElementChecker.isFinished(caseData);
        assert (!isFinished);
    }

    @Test
    public void finishedWithFieldValuesYes() {

        CaseData caseData = CaseData.builder()
            .habitualResidentInOtherState(yes)
            .habitualResidentInOtherStateGiveReason("reason")
            .jurisdictionIssue(yes)
            .jurisdictionIssueGiveReason("reason")
            .requestToForeignAuthority(yes)
            .requestToForeignAuthorityGiveReason("reason")
            .build();
        boolean isFinished = internationalElementChecker.isFinished(caseData);
        assert (isFinished);
    }

    @Test
    public void notStartedWithBlankOrEmptyFieldValues() {

        CaseData caseData = CaseData.builder().habitualResidentInOtherState(null).jurisdictionIssue(null)
            .requestToForeignAuthority(null).build();
        boolean isStarted = internationalElementChecker.isStarted(caseData);
        assert !(isStarted);
    }

    @Test
    public void notStartedWithUnattendedFieldValues() {
        CaseData caseData = CaseData.builder().build();
        boolean isStarted = internationalElementChecker.isStarted(caseData);
        assert (!isStarted);
    }

}
