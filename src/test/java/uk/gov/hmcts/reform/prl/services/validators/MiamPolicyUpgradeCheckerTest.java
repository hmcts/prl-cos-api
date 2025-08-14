package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

@RunWith(MockitoJUnitRunner.class)
public class MiamPolicyUpgradeCheckerTest {

    @InjectMocks
    MiamPolicyUpgradeChecker miamPolicyUpgradeChecker;

    @Mock
    TaskErrorService taskErrorService;

    @Test
    public void testHasMandatoryCompleted() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void testGetDefaultTaskState() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .build())
            .build();
        Assert.assertEquals(TaskState.NOT_STARTED, miamPolicyUpgradeChecker.getDefaultTaskState(caseData));
    }

    @Test
    public void testIsStartedFalse() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isStarted(caseData));
    }

    @Test
    public void testIsStartedTrue() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.Yes)
                .build())
            .build();
        Assert.assertTrue(miamPolicyUpgradeChecker.isStarted(caseData));
    }

    @Test
    public void testIsFinished() {
        CaseData caseData = CaseData.builder()
            .consentOrder(YesOrNo.No)
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedHasConstantOrder() {
        CaseData caseData = CaseData.builder()
            .consentOrder(YesOrNo.Yes)
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildInvolvedInMiam() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.Yes)
                .build())
            .build();
        Assert.assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiam() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedEverythingEmpty() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.Yes)
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedRegNumberEmpty() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.Yes)
                .mediatorRegistrationNumber("")
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedMediatorServiceNameEmpty() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.Yes)
                .mediatorRegistrationNumber("test")
                .familyMediatorServiceName("")
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedMediatorSoleNameEmpty() {
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
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedMediatorSoleNameEverythingThere() {
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
        Assert.assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionNo() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.No)
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionYes() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionEmpty() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(new ArrayList<>())
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionDomesticAbuse() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionDomesticAbuseEmpty() {
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
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionDomesticAbuseEmptyEvidence() {
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
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionDomesticAbuseSuccess() {
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
        Assert.assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionChildProtection() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuChildProtectionConcern))
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionChildProtectionSuccess() {
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
        Assert.assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionUrgency() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuUrgency))
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionUrgencySuccess() {
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
        Assert.assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionPreviousMiam() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionPreviousMiamAttendence1() {
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
        Assert.assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionPreviousMiamAttendence2Certificate() {
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
        Assert.assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionPreviousMiamAttendence2Details() {
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
        Assert.assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionOther() {
        CaseData caseData = CaseData.builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuChildInvolvedInMiam(YesOrNo.No)
                .mpuApplicantAttendedMiam(YesOrNo.No)
                .mpuClaimingExemptionMiam(YesOrNo.Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuOther))
                .build())
            .build();
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionOtherReason1() {
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
        Assert.assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionOtherReason3() {
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
        Assert.assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionOtherReason4() {
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
        Assert.assertFalse(miamPolicyUpgradeChecker.isFinished(caseData));
    }

    @Test
    public void testIsFinishedChildNotInvolvedInMiamApplicantAttendedClaimingExemptionOtherReason5() {
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
        Assert.assertTrue(miamPolicyUpgradeChecker.isFinished(caseData));
    }
}
