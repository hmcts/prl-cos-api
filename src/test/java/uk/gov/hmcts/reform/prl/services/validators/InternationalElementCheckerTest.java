package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

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
        assertFalse(hasMandatory);
    }

    @Test
    public void notFinishedWithEmptyFields() {

        CaseData caseData = CaseData.builder().build();
        boolean isFinished = internationalElementChecker.isFinished(caseData);
        assertFalse(isFinished);
    }

    @Test
    public void notFinishedWithFieldValuesYes() {

        CaseData caseData = CaseData.builder().habitualResidentInOtherState(Yes).jurisdictionIssue(Yes)
            .requestToForeignAuthority(Yes).build();
        boolean isFinished = internationalElementChecker.isFinished(caseData);
        assertFalse(isFinished);
    }

    @Test
    public void finishedWithFieldValuesYes() {

        CaseData caseData = CaseData.builder()
            .habitualResidentInOtherState(Yes)
            .habitualResidentInOtherStateGiveReason("reason")
            .jurisdictionIssue(Yes)
            .jurisdictionIssueGiveReason("reason")
            .requestToForeignAuthority(Yes)
            .requestToForeignAuthorityGiveReason("reason")
            .build();
        boolean isFinished = internationalElementChecker.isFinished(caseData);
        assertTrue(isFinished);
    }

    @Test
    public void notStartedWithBlankOrEmptyFieldValues() {

        CaseData caseData = CaseData.builder().habitualResidentInOtherState(null).jurisdictionIssue(null)
            .requestToForeignAuthority(null).build();
        boolean isStarted = internationalElementChecker.isStarted(caseData);
        assertFalse(isStarted);
    }

    @Test
    public void notStartedWithUnattendedFieldValues() {
        CaseData caseData = CaseData.builder().build();
        boolean isStarted = internationalElementChecker.isStarted(caseData);
        assertFalse(isStarted);
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(internationalElementChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
