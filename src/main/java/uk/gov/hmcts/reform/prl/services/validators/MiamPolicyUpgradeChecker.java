package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.MIAM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MiamPolicyUpgradeChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        boolean finished = false;

        Optional<YesOrNo> childInvolvedInMiam = ofNullable(caseData.getMiamPolicyUpgradeDetails().getChildInvolvedInMiam());
        Optional<YesOrNo> applicantAttendedMiam = ofNullable(caseData.getMiamPolicyUpgradeDetails().getApplicantAttendedMiam());
        Optional<YesOrNo> claimingExemptionMiam = ofNullable(caseData.getMiamPolicyUpgradeDetails().getClaimingExemptionMiam());

        if (childInvolvedInMiam.isPresent() && Yes.equals(childInvolvedInMiam.get())) {
            finished = true;
        } else if (childInvolvedInMiam.isPresent() && No.equals(childInvolvedInMiam.get())) {
            finished = inspectChildInvolvedInMiamNoFlow(
                caseData,
                applicantAttendedMiam,
                finished,
                claimingExemptionMiam
            );
        }
        if (finished) {
            taskErrorService.removeError(MIAM_ERROR);
            return true;
        }
        Optional<YesOrNo> hasConsentOrder = ofNullable(caseData.getConsentOrder());
        taskErrorService.addEventError(MIAM, MIAM_ERROR, MIAM_ERROR.getError());
        if (hasConsentOrder.isPresent() && YesOrNo.Yes.equals(hasConsentOrder.get())) {
            taskErrorService.removeError(MIAM_ERROR);
        }
        return false;
    }

    private static boolean inspectChildInvolvedInMiamNoFlow(CaseData caseData, Optional<YesOrNo> applicantAttendedMiam, boolean finished, Optional<YesOrNo> claimingExemptionMiam) {
        if (applicantAttendedMiam.isPresent()) {
            if (Yes.equals(applicantAttendedMiam.get())) {
                finished = hasProvidedMiamCertificate(caseData);
            } else if (claimingExemptionMiam.isPresent() && Yes.equals(claimingExemptionMiam.get())) {
                finished = hasProvidedMiamCertificate(caseData);
            } else if (claimingExemptionMiam.isPresent() && No.equals(claimingExemptionMiam.get())) {
                //TODO: Rest of the logic to go here for No, No & No topic
                finished = true;
            }
        }
        return finished;
    }

    private static boolean hasProvidedMiamCertificate(CaseData caseData) {
        boolean finished;
        Optional<String> mediatorRegNumber = ofNullable(caseData.getMiamPolicyUpgradeDetails().getMediatorRegistrationNumber());
        Optional<String> mediatorServiceName = ofNullable(caseData.getMiamPolicyUpgradeDetails().getFamilyMediatorServiceName());
        Optional<String> mediatorSoleTrader = ofNullable(caseData.getMiamPolicyUpgradeDetails().getSoleTraderName());
        Optional<Document> miamCertDocument = ofNullable(caseData.getMiamPolicyUpgradeDetails().getMiamCertificationDocumentUpload());
        finished = mediatorRegNumber.isPresent()
            && StringUtils.isNotEmpty(mediatorRegNumber.get().trim())
            && mediatorServiceName.isPresent()
            && StringUtils.isNotEmpty(mediatorServiceName.get().trim())
            && mediatorSoleTrader.isPresent()
            && StringUtils.isNotEmpty(mediatorSoleTrader.get().trim())
            && miamCertDocument.isPresent();
        return finished;
    }


    @Override
    public boolean isStarted(CaseData caseData) {
        return anyNonEmpty(
            caseData.getMiamPolicyUpgradeDetails().getChildInvolvedInMiam()
        );
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
