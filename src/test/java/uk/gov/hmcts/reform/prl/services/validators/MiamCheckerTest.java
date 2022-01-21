package uk.gov.hmcts.reform.prl.services.validators;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.documents.MiamDocument;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum.MIAMChildProtectionConcernChecklistEnum_value_1;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.childProtectionConcern;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.domesticViolence;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.no;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.yes;


@RunWith(MockitoJUnitRunner.class)
public class MiamCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    MiamChecker miamChecker;

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assert !miamChecker.isStarted(caseData);
    }

    @Test
    public void whenBasicMiamCaseDataPresentThenIsStartedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .applicantAttendedMiam(yes)
            .claimingExemptionMiam(no)
            .familyMediatorMiam(no)
            .build();


        assert miamChecker.isStarted(caseData);
    }

    @Test
    public void whenNoDataHasMandatoryCompletedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assert !miamChecker.hasMandatoryCompleted(caseData);
    }

    @Test
    public void whenNoDataIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assert !miamChecker.isFinished(caseData);
    }

    @Test
    public void whenApplicantHasAttendedMiamAndDetailsProvidedIsFinishedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .applicantAttendedMiam(yes)
            .mediatorRegistrationNumber("123456")
            .familyMediatorServiceName("Test Name")
            .soleTraderName("Trade Sole")
            .miamCertificationDocumentUpload(MiamDocument.builder().build())
            .build();

        assert miamChecker.isFinished(caseData);
    }

    @Test
    public void whenApplicantHasNotAttendedMiamButHasApprovedExemptionIsFinishedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .applicantAttendedMiam(no)
            .claimingExemptionMiam(yes)
            .familyMediatorMiam(yes)
            .mediatorRegistrationNumber1("123456")
            .familyMediatorServiceName1("Test Name")
            .soleTraderName1("Trade Sole")
            .miamCertificationDocumentUpload1(MiamDocument.builder().build())
            .build();

        assert miamChecker.isFinished(caseData);
    }

    @Test
    public void whenApplicantHasNotAttendedMiamButHasCompletedExemptionsSectionIsFinishedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .applicantAttendedMiam(no)
            .claimingExemptionMiam(yes)
            .familyMediatorMiam(no)
            .miamExemptionsChecklist(Collections.singletonList(domesticViolence))
            .miamDomesticViolenceChecklist(Collections.singletonList(miamDomesticViolenceChecklistEnum_Value_1))
            .build();

            assert miamChecker.isFinished(caseData);

    }

    @Test
    public void whenApplicantHasNotAttendedMiamButHasCompletedExemptionsSectionSubmittedChildProtectionConcernIsFinishedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .applicantAttendedMiam(no)
            .claimingExemptionMiam(yes)
            .familyMediatorMiam(no)
            .miamExemptionsChecklist(Collections.singletonList(childProtectionConcern))
            .miamChildProtectionConcernList(Collections.singletonList(MIAMChildProtectionConcernChecklistEnum_value_1))
            .build();

        assertTrue(miamChecker.isFinished(caseData));

    }


}
