package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPolicyUpgradeChildProtectionConcernEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class MiamPolicyUpgradeCheckerTest {

    @InjectMocks
    MiamPolicyUpgradeChecker miamPolicyUpgradeChecker;

    @Mock
    TaskErrorService taskErrorService;

    @Test
    void testHasMandatoryCompleted() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void testGetDefaultTaskState() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .build())
            .build();
        assertEquals(TaskState.NOT_STARTED, miamPolicyUpgradeChecker.getDefaultTaskState(caseData));
    }

    @Test
    void testIsStartedFalse() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isStarted(caseData));
    }

    @Test
    void testIsStartedTrue() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.Yes)
                .build())
            .build();
        assertTrue(miamPolicyUpgradeChecker.isStarted(caseData));
    }

    @Test
    void testIsFinished() {
        CaseData caseData = CaseData.builder()
            .consentOrder(YesOrNo.No)
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedHasConstantOrder() {
        CaseData caseData = CaseData.builder()
            .consentOrder(YesOrNo.Yes)
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildInvolvedInMiam() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.Yes)
                .build())
            .build();
        assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiam() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedEverythingEmpty() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.Yes)
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedRegNumberEmpty() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.Yes)
                .mediatorRegistrationNumber("")
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedMediatorServiceNameEmpty() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.Yes)
                .mediatorRegistrationNumber("test")
                .familyMediatorServiceName("")
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedMediatorSoleNameEmpty() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.Yes)
                .mediatorRegistrationNumber("test")
                .familyMediatorServiceName("test")
                .soleTraderName("")
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedMediatorSoleNameEverythingThere() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.Yes)
                .mediatorRegistrationNumber("test")
                .familyMediatorServiceName("test")
                .soleTraderName("test")
                .miamCertificationDocumentUpload(Document.builder().build())
                .build())
            .build();
        assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionNo() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.No)
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionYes() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionEmpty() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(new ArrayList<>())
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionDomesticAbuse() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionDomesticAbuseEmpty() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuDomesticAbuseEvidences(new ArrayList<>())
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionDomesticAbuseEmptyEvidence() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuDomesticAbuseEvidences(List.of(MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1))
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionDomesticAbuseSuccess() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuDomesticAbuseEvidences(List.of(MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1))
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
                .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
                .build())
            .build();
        assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionChildProtection() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuChildProtectionConcern))
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionChildProtectionSuccess() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuChildProtectionConcern))
                .mpuChildProtectionConcernReason(MiamPolicyUpgradeChildProtectionConcernEnum.mpuChildProtectionConcern_value_1)
                .build())
            .build();
        assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionUrgency() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuUrgency))
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionUrgencySuccess() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuUrgency))
                .mpuUrgencyReason(MiamUrgencyReasonChecklistEnum.miamPolicyUpgradeUrgencyReason_Value_1)
                .build())
            .build();
        assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionPreviousMiam() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionPreviousMiamAttendence1() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                .mpuPreviousMiamAttendanceReason(MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_1)
                .mpuDocFromDisputeResolutionProvider(Document.builder().build())
                .build())
            .build();
        assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionPreviousMiamAttendence2Certificate() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                .mpuPreviousMiamAttendanceReason(MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_2)
                .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamCertificate)
                .mpuCertificateByMediator(Document.builder().build())
                .build())
            .build();
        assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionPreviousMiamAttendence2Details() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                .mpuPreviousMiamAttendanceReason(MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_2)
                .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamAttendanceDetails)
                .mpuMediatorDetails("test")
                .build())
            .build();
        assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionOther() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuOther))
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionOtherReason1() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuOther))
                .mpuOtherExemptionReasons(MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_1)
                .build())
            .build();
        assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionOtherReason3() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuOther))
                .mpuOtherExemptionReasons(MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_3)
                .mpuApplicantUnableToAttendMiamReason1("test")
                .build())
            .build();
        assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionOtherReason4() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuOther))
                .mpuOtherExemptionReasons(MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_4)
                .mpuApplicantUnableToAttendMiamReason1("")
                .build())
            .build();
        assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionOtherReason5() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuOther))
                .mpuOtherExemptionReasons(MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_5)
                .mpuApplicantUnableToAttendMiamReason2("test")
                .build())
            .build();
        assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }
}
