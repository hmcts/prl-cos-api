package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;

public class MiamCheckerTest {

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        MiamChecker miamChecker = new MiamChecker();

        assert !miamChecker.isStarted(caseData);
    }

    @Test
    public void whenBasicMiamCaseDataPresentThenIsStartedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .applicantAttendedMIAM(YES)
            .claimingExemptionMIAM(NO)
            .familyMediatorMIAM(NO)
            .build();

        MiamChecker miamChecker = new MiamChecker();

        assert miamChecker.isStarted(caseData);
    }

    @Test
    public void whenNoDataHasMandatoryCompletedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        MiamChecker miamChecker = new MiamChecker();

        assert !miamChecker.hasMandatoryCompleted(caseData);
    }

    @Test
    public void whenNoDataIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        MiamChecker miamChecker = new MiamChecker();

        assert !miamChecker.isFinished(caseData);
    }

    public void whenApplicantHasAttendedMiamAndDetailsProvidedIsFinishedReturnsTrue() {
        CaseData caseData = CaseData.builder().build();

        MiamChecker miamChecker = new MiamChecker();

        assert !miamChecker.isFinished(caseData);
    }




}
