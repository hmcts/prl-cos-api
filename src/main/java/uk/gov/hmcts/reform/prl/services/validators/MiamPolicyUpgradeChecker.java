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

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MiamPolicyUpgradeChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {
        log.info("verifying isFinished for miam");
        boolean finished = false;

        Optional<YesOrNo> childInvolvedInMiam = ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuChildInvolved());
        Optional<YesOrNo> applicantAttendedMiam = ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuApplicantAttended());
        Optional<YesOrNo> claimingExemptionMiam = ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuClaimingExemption());
        log.info("childInvolvedInMiam.isPresent() {}", childInvolvedInMiam.isPresent());
        log.info("applicantAttendedMiam.isPresent() {}", applicantAttendedMiam.isPresent());
        log.info("claimingExemptionMiam.isPresent() {}", claimingExemptionMiam.isPresent());
        if (childInvolvedInMiam.isPresent() && Yes.equals(childInvolvedInMiam.get())) {
            log.info("claimingExemptionMiam is Yes");
            finished = true;
        } else if (childInvolvedInMiam.isPresent() && No.equals(childInvolvedInMiam.get())) {
            log.info("claimingExemptionMiam is No");
            finished = inspectChildInvolvedInMiamNoFlow(
                caseData,
                applicantAttendedMiam,
                claimingExemptionMiam
            );
            log.info("Dont know");
        }
        if (finished) {
            log.info("all done, its done");
            taskErrorService.removeError(MIAM_POLICY_UPGRADE_ERROR);
            return true;
        }
        Optional<YesOrNo> hasConsentOrder = ofNullable(caseData.getConsentOrder());
        taskErrorService.addEventError(
            MIAM_POLICY_UPGRADE,
            MIAM_POLICY_UPGRADE_ERROR,
            MIAM_POLICY_UPGRADE_ERROR.getError()
        );
        if (hasConsentOrder.isPresent() && YesOrNo.Yes.equals(hasConsentOrder.get())) {
            taskErrorService.removeError(MIAM_POLICY_UPGRADE_ERROR);
        }
        log.info("nope, something is wrong");
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
        log.info("finished in inspectChildInvolvedInMiamNoFlow {}", finished);
        return finished;
    }

    private boolean hasClaimedExemption(CaseData caseData) {
        boolean finished = true;
        Optional<List<MiamExemptionsChecklistEnum>> miamPolicyUpgradeExemptionsChecklist
            = Optional.ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons());
        if (!miamPolicyUpgradeExemptionsChecklist.isPresent()) {
            finished = false;
        } else {
            finished = checkedForClaimedExemptions(caseData);
        }

        return finished;
    }

    private boolean checkedForClaimedExemptions(CaseData caseData) {
        boolean finished;
        List<MiamExemptionsChecklistEnum> miamPolicyUpgradeExemptionsChecklist
            = caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons();

        if (miamPolicyUpgradeExemptionsChecklist.contains(MiamExemptionsChecklistEnum.DOMESTIC_ABUSE)) {
            finished = checkForDomesticAbuseExemption(caseData);
        } else if (miamPolicyUpgradeExemptionsChecklist.contains(MiamExemptionsChecklistEnum.CHILD_PROTECTION_CONCERN)) {
            finished = checkForChildProtectionConcernExemption(caseData);
        } else if (miamPolicyUpgradeExemptionsChecklist.contains(MiamExemptionsChecklistEnum.URGENCY)) {
            finished = checkForUrgencyExemption(caseData);
        } else if (miamPolicyUpgradeExemptionsChecklist.contains(MiamExemptionsChecklistEnum.PREVIOUS_MIAM_ATTENDANCE)) {
            finished = checkForPreviousMiamAttendanceExemption(caseData);
        } else {
            finished = checkedForOtherExemptions(caseData);
        }

        return finished;
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
        if (ObjectUtils.isEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons())
            || ((MiamOtherGroundsChecklistEnum.MIAM_POLICY_UPGRADE_OTHER_GROUNDS_CHECKLIST_ENUM_3.equals(
            caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons())
            || MiamOtherGroundsChecklistEnum.MIAM_POLICY_UPGRADE_OTHER_GROUNDS_CHECKLIST_ENUM_4.equals(
            caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons()))
            && ObjectUtils.isEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuApplicantUnableToAttendMiamReason1()))
            || (MiamOtherGroundsChecklistEnum.MIAM_POLICY_UPGRADE_OTHER_GROUNDS_CHECKLIST_ENUM_5.equals(
            caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons())
            && ObjectUtils.isEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuApplicantUnableToAttendMiamReason2()))) {
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
        if (ObjectUtils.isEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
            || (MiamPreviousAttendanceChecklistEnum.MIAM_PREV_ATT_CHK_LIST_ENUM_VALUE_1.equals(
            caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
            && ObjectUtils.isEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuDocFromDisputeResolutionProvider()))
            || (MiamPreviousAttendanceChecklistEnum.MIAM_PREV_ATT_CHK_LIST_ENUM_VALUE_2.equals(
            caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
            && ObjectUtils.isEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence()))
            || (ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence())
            && ObjectUtils.isEmpty(
            caseData.getMiamPolicyUpgradeDetails().getMpuCertificateByMediator()))
            || (ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence())
            && ObjectUtils.isEmpty(
            caseData.getMiamPolicyUpgradeDetails().getMpuMediatorDetails()))) {
            finished = false;
        }

        return finished;
    }

    private boolean checkForDomesticAbuseExemption(CaseData caseData) {
        boolean finished = true;
        Optional<List<MiamDomesticAbuseChecklistEnum>> miamDomesticAbuseEvidenceList
            = Optional.ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuDomesticAbuseEvidences());
        if (!miamDomesticAbuseEvidenceList.isPresent()
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
        log.info("verifying isStarted for miam");
        Optional<YesOrNo> childInvolvedInMiam = ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuChildInvolved());
        log.info("verifying isStarted for miam {}", childInvolvedInMiam.isPresent());
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
