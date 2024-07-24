package uk.gov.hmcts.reform.prl.services.validators;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamDetails;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum.MIAMChildProtectionConcernChecklistEnum_value_1;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.childProtectionConcern;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.domesticViolence;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;


@RunWith(MockitoJUnitRunner.class)
public class MiamCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    MiamChecker miamChecker;

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder()
            .miamDetails(MiamDetails.builder().build()).build();

        assertTrue(!miamChecker.isStarted(caseData));
    }

    @Test
    public void whenBasicMiamCaseDataPresentThenIsStartedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .miamDetails(MiamDetails.builder()
                             .applicantAttendedMiam(Yes)
                             .claimingExemptionMiam(No)
                             .familyMediatorMiam(No)
                             .build())
            .build();


        assertTrue(miamChecker.isStarted(caseData));
    }

    @Test
    public void whenNoDataHasMandatoryCompletedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertTrue(!miamChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenNoDataIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder().miamDetails(
            MiamDetails.builder().build()
        ).build();

        assertTrue(!miamChecker.isFinished(caseData));
    }

    @Test
    public void whenApplicantHasAttendedMiamAndDetailsProvidedIsFinishedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .miamDetails(MiamDetails.builder()
                             .applicantAttendedMiam(Yes)
                             .mediatorRegistrationNumber("123456")
                             .familyMediatorServiceName("Test Name")
                             .soleTraderName("Trade Sole")
                             .miamCertificationDocumentUpload(Document.builder().build().builder().build())
                             .build())
            .build();

        assertTrue(miamChecker.isFinished(caseData));
    }

    @Test
    public void whenApplicantHasNotAttendedMiamButHasApprovedExemptionIsFinishedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .miamDetails(MiamDetails.builder()
                             .applicantAttendedMiam(No)
                             .claimingExemptionMiam(Yes)
                             .familyMediatorMiam(Yes)
                             .mediatorRegistrationNumber1("123456")
                             .familyMediatorServiceName1("Test Name")
                             .soleTraderName1("Trade Sole")
                             .miamCertificationDocumentUpload1(Document.builder().build())
                             .build())
            .build();

        assertTrue(miamChecker.isFinished(caseData));
    }

    @Test
    public void whenApplicantHasNotAttendedMiamButHasCompletedExemptionsSectionIsFinishedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .miamDetails(MiamDetails.builder()
                             .applicantAttendedMiam(No)
                             .claimingExemptionMiam(Yes)
                             .familyMediatorMiam(No)
                             .miamExemptionsChecklist(Collections.singletonList(domesticViolence))
                             .miamDomesticViolenceChecklist(Collections.singletonList(miamDomesticViolenceChecklistEnum_Value_1))
                             .build())

            .build();

        assertTrue(miamChecker.isFinished(caseData));
    }

    @Test
    public void whenApplicantHasNotAttendedMiamButHasCompletedExemptionsSectionSubmittedChildProtectionConcernIsFinishedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .miamDetails(MiamDetails.builder()
                             .applicantAttendedMiam(No)
                             .claimingExemptionMiam(Yes)
                             .familyMediatorMiam(No)
                             .miamExemptionsChecklist(Collections.singletonList(childProtectionConcern))
                             .miamChildProtectionConcernList(Collections.singletonList(MIAMChildProtectionConcernChecklistEnum_value_1))
                             .build())

            .build();

        assertTrue(miamChecker.isFinished(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(miamChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
