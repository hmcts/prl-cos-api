package uk.gov.hmcts.reform.prl.services.validators;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamDetails;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum.MIAMChildProtectionConcernChecklistEnum_value_1;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.childProtectionConcern;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.domesticViolence;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;


@ExtendWith(MockitoExtension.class)
class MiamCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    MiamChecker miamChecker;

    @Test
    void whenNoCaseDataThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder()
            .miamDetails(MiamDetails.builder().build()).build();

        assertFalse(miamChecker.isStarted(caseData));
    }

    @Test
    void whenBasicMiamCaseDataPresentThenIsStartedReturnsTrue() {
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
    void whenNoDataHasMandatoryCompletedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(miamChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenNoDataIsFinishedReturnsFalse() {
        CaseData caseData = CaseData.builder().miamDetails(
            MiamDetails.builder().build()
        ).build();

        assertFalse(miamChecker.isFinished(caseData));
    }

    @Test
    void whenApplicantHasAttendedMiamAndDetailsProvidedIsFinishedReturnsTrue() {
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
    void whenApplicantHasNotAttendedMiamButHasApprovedExemptionIsFinishedReturnsTrue() {
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
    void whenApplicantHasNotAttendedMiamButHasCompletedExemptionsSectionIsFinishedReturnsTrue() {
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
    void whenApplicantHasNotAttendedMiamButHasCompletedExemptionsSectionSubmittedChildProtectionConcernIsFinishedReturnsTrue() {
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
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(miamChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
