package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM_POLICY_UPGRADE;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.MIAM_POLICY_UPGRADE_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum.miamAttendanceDetails;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum.miamCertificate;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MiamPolicyUpgradeChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {
        boolean finished = false;

        Optional<YesOrNo> childInvolvedInMiam = ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuChildInvolvedInMiam());
        Optional<YesOrNo> applicantAttendedMiam = ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuApplicantAttendedMiam());
        Optional<YesOrNo> claimingExemptionMiam = ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuClaimingExemptionMiam());
        if (childInvolvedInMiam.isPresent() && Yes.equals(childInvolvedInMiam.get())) {
            finished = true;
        } else if (childInvolvedInMiam.isPresent() && No.equals(childInvolvedInMiam.get())) {
            finished = inspectChildInvolvedInMiamNoFlow(
                caseData,
                applicantAttendedMiam,
                claimingExemptionMiam
            );
        }
        log.info("verifying isFinished for MIAM Policy Upgrade {}", finished);
        if (finished) {
            taskErrorService.removeError(MIAM_POLICY_UPGRADE_ERROR);
            return true;
        }
        Optional<YesOrNo> hasConsentOrder = ofNullable(caseData.getConsentOrder());
        taskErrorService.addEventError(
            MIAM_POLICY_UPGRADE,
            MIAM_POLICY_UPGRADE_ERROR,
            MIAM_POLICY_UPGRADE_ERROR.getError()
        );
        if (hasConsentOrder.isPresent() && YesOrNo.Yes.equals(hasConsentOrder.get()) && !isStarted(caseData)) {
            taskErrorService.removeError(MIAM_POLICY_UPGRADE_ERROR);
        }
        return false;
    }

    private boolean inspectChildInvolvedInMiamNoFlow(CaseData caseData,
                                                     Optional<YesOrNo> applicantAttendedMiam,
                                                     Optional<YesOrNo> claimingExemptionMiam) {
        boolean finished = false;
        if (applicantAttendedMiam.isPresent()) {
            if (Yes.equals(applicantAttendedMiam.get())) {
                finished = hasProvidedMiamCertificate(caseData);
            } else if (claimingExemptionMiam.isPresent() && Yes.equals(claimingExemptionMiam.get())) {
                finished = hasClaimedExemption(caseData);
            }
        }
        return finished;
    }

    private boolean hasClaimedExemption(CaseData caseData) {
        boolean finished;
        Optional<List<MiamExemptionsChecklistEnum>> miamPolicyUpgradeExemptionsChecklist
            = Optional.ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons());
        if (!miamPolicyUpgradeExemptionsChecklist.isPresent() || miamPolicyUpgradeExemptionsChecklist.get().isEmpty()) {
            finished = false;
        } else {
            finished = checkedForClaimedExemptions(caseData);
        }
        return finished;
    }

    private boolean checkedForClaimedExemptions(CaseData caseData) {
        boolean domesticAbuseFinished = true;
        boolean childProtectionFinished = true;
        boolean urgencyFinished = true;
        boolean previousMiamAttendanceFinished = true;
        boolean otherFinished = true;
        List<MiamExemptionsChecklistEnum> miamPolicyUpgradeExemptionsChecklist
            = caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons();

        if (miamPolicyUpgradeExemptionsChecklist.contains(MiamExemptionsChecklistEnum.mpuDomesticAbuse)) {
            domesticAbuseFinished = checkForDomesticAbuseExemption(caseData);
        }
        if (miamPolicyUpgradeExemptionsChecklist.contains(MiamExemptionsChecklistEnum.mpuChildProtectionConcern)) {
            childProtectionFinished = checkForChildProtectionConcernExemption(caseData);
        }
        if (miamPolicyUpgradeExemptionsChecklist.contains(MiamExemptionsChecklistEnum.mpuUrgency)) {
            urgencyFinished = checkForUrgencyExemption(caseData);
        }
        if (miamPolicyUpgradeExemptionsChecklist.contains(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance)) {
            previousMiamAttendanceFinished = checkForPreviousMiamAttendanceExemption(caseData);
        }
        if (miamPolicyUpgradeExemptionsChecklist.contains(MiamExemptionsChecklistEnum.mpuOther)) {
            otherFinished = checkedForOtherExemptions(caseData);
        }
        return domesticAbuseFinished && childProtectionFinished && urgencyFinished
            && previousMiamAttendanceFinished && otherFinished;
    }

    private boolean checkForChildProtectionConcernExemption(CaseData caseData) {
        boolean finished = true;
        if (ObjectUtils.isEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuChildProtectionConcernReason())) {
            finished = false;
        }
        return finished;
    }

    private boolean checkedForOtherExemptions(CaseData caseData) {
        boolean finished = true;
        Optional<String> mpuApplicantUnableToAttendMiamReason1 = ofNullable(caseData.getMiamPolicyUpgradeDetails()
                                                                                .getMpuApplicantUnableToAttendMiamReason1());
        boolean mpuApplicantUnableToAttendMiamReason1Present = mpuApplicantUnableToAttendMiamReason1.isPresent()
            && StringUtils.isNotEmpty(mpuApplicantUnableToAttendMiamReason1.get().trim());

        Optional<String> mpuApplicantUnableToAttendMiamReason2 = ofNullable(caseData.getMiamPolicyUpgradeDetails()
                                                                                .getMpuApplicantUnableToAttendMiamReason2());
        boolean mpuApplicantUnableToAttendMiamReason2Present = mpuApplicantUnableToAttendMiamReason2.isPresent()
            && StringUtils.isNotEmpty(mpuApplicantUnableToAttendMiamReason2.get().trim());

        if (ObjectUtils.isEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons())
            || ((MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_3.equals(
            caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons())
            || MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_4.equals(
            caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons()))
            && !mpuApplicantUnableToAttendMiamReason1Present)
            || (MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_5.equals(
            caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons())
            && !mpuApplicantUnableToAttendMiamReason2Present)) {
            finished = false;
        }
        return finished;
    }

    private boolean checkForUrgencyExemption(CaseData caseData) {
        boolean finished = true;
        if (ObjectUtils.isEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuUrgencyReason())) {
            finished = false;
        }
        return finished;
    }

    private boolean checkForPreviousMiamAttendanceExemption(CaseData caseData) {
        boolean finished = true;
        Optional<String> mpuMediatorDetails = ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuMediatorDetails());
        boolean mpuMediatorDetailsPresent = mpuMediatorDetails.isPresent()
            && StringUtils.isNotEmpty(mpuMediatorDetails.get().trim());

        if (ObjectUtils.isEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
            || (MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_1.equals(
            caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
            && ObjectUtils.isEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuDocFromDisputeResolutionProvider()))
            || (MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_2.equals(
            caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
            && ObjectUtils.isEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence()))
            || (miamCertificate.equals(caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence())
            && ObjectUtils.isEmpty(
            caseData.getMiamPolicyUpgradeDetails().getMpuCertificateByMediator()))
            || (miamAttendanceDetails.equals(caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence())
            && !mpuMediatorDetailsPresent)) {
            finished = false;
        }
        return finished;
    }

    private boolean checkForDomesticAbuseExemption(CaseData caseData) {
        boolean finished = true;
        Optional<List<MiamDomesticAbuseChecklistEnum>> miamDomesticAbuseEvidenceList
            = Optional.ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuDomesticAbuseEvidences());
        if (!miamDomesticAbuseEvidenceList.isPresent()
            || miamDomesticAbuseEvidenceList.get().isEmpty()
            || ObjectUtils.isEmpty(
            caseData.getMiamPolicyUpgradeDetails().getMpuIsDomesticAbuseEvidenceProvided())) {
            finished = false;
        }
        return finished;
    }

    private static boolean hasProvidedMiamCertificate(CaseData caseData) {
        Optional<String> mediatorRegNumber = ofNullable(caseData.getMiamPolicyUpgradeDetails().getMediatorRegistrationNumber());
        Optional<String> mediatorServiceName = ofNullable(caseData.getMiamPolicyUpgradeDetails().getFamilyMediatorServiceName());
        Optional<String> mediatorSoleTrader = ofNullable(caseData.getMiamPolicyUpgradeDetails().getSoleTraderName());
        Optional<Document> miamCertDocument = ofNullable(caseData.getMiamPolicyUpgradeDetails().getMiamCertificationDocumentUpload());
        return mediatorRegNumber.isPresent()
            && StringUtils.isNotEmpty(mediatorRegNumber.get().trim())
            && mediatorServiceName.isPresent()
            && StringUtils.isNotEmpty(mediatorServiceName.get().trim())
            && mediatorSoleTrader.isPresent()
            && StringUtils.isNotEmpty(mediatorSoleTrader.get().trim())
            && miamCertDocument.isPresent();
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        Optional<YesOrNo> childInvolvedInMiam = ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuChildInvolvedInMiam());
        log.info("verifying isStarted for MIAM Policy Upgrade {}", childInvolvedInMiam.isPresent());
        return childInvolvedInMiam.isPresent();
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }

}
