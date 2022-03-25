package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.MIAM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.childProtectionConcern;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.domesticViolence;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.other;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.previousMIAMattendance;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.urgency;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;


@Service
public class MiamChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        boolean finished = false;

        Optional<YesOrNo> applicantAttendedMiam = ofNullable(caseData.getApplicantAttendedMiam());
        Optional<YesOrNo> claimingExemptionMiam = ofNullable(caseData.getClaimingExemptionMiam());
        Optional<YesOrNo> familyMediatiorMiam = ofNullable(caseData.getFamilyMediatorMiam());

        Optional<String> mediatorRegNumber = ofNullable(caseData.getMediatorRegistrationNumber());
        Optional<String> mediatorServiceName = ofNullable(caseData.getFamilyMediatorServiceName());
        Optional<String> mediatorSoleTrader = ofNullable(caseData.getSoleTraderName());
        Optional<MiamDocument> miamCertDocument = ofNullable(caseData.getMiamCertificationDocumentUpload());
        Optional<YesOrNo> hasConsentOrder = ofNullable(caseData.getConsentOrder());

        if (applicantAttendedMiam.isPresent() && applicantAttendedMiam.get().equals(Yes)) {
            finished = mediatorRegNumber.isPresent()
                && mediatorServiceName.isPresent()
                && mediatorSoleTrader.isPresent()
                && miamCertDocument.isPresent();

        } else if ((applicantAttendedMiam.isPresent() && applicantAttendedMiam.get().equals(No))
            && (claimingExemptionMiam.isPresent() && claimingExemptionMiam.get().equals(Yes))
            && (familyMediatiorMiam.isPresent() && familyMediatiorMiam.get().equals(Yes))) {

            Optional<String> mediatorRegNumber1 = ofNullable(caseData.getMediatorRegistrationNumber1());
            Optional<String> mediatorServiceName1 = ofNullable(caseData.getFamilyMediatorServiceName1());
            Optional<String> mediatorSoleTrader1 = ofNullable(caseData.getSoleTraderName1());
            Optional<Document> miamCertDocument1 = ofNullable(caseData.getMiamCertificationDocumentUpload1());

            finished = mediatorRegNumber1.isPresent()
                && mediatorServiceName1.isPresent()
                && mediatorSoleTrader1.isPresent()
                && miamCertDocument1.isPresent();

        } else {
            Optional<List<MiamExemptionsChecklistEnum>> exceptions = ofNullable(caseData.getMiamExemptionsChecklist());
            if (exceptions.isPresent()) {
                finished =  checkMiamExemptions(caseData);
            }
        }
        taskErrorService.addEventError(MIAM, MIAM_ERROR, MIAM_ERROR.getError());
        if (finished) {
            taskErrorService.removeError(MIAM_ERROR);
            return true;
        }
        if (hasConsentOrder.isPresent() && YesOrNo.Yes.equals(hasConsentOrder.get())) {
            taskErrorService.removeError(MIAM_ERROR);
        }
        return false;
    }



    @Override
    public boolean isStarted(CaseData caseData) {
        return anyNonEmpty(
            caseData.getApplicantAttendedMiam(),
            caseData.getClaimingExemptionMiam(),
            caseData.getFamilyMediatorMiam()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    public boolean checkMiamExemptions(CaseData caseData) {

        Optional<List<MiamExemptionsChecklistEnum>> exceptions = ofNullable(caseData.getMiamExemptionsChecklist());

        boolean dvCompleted = true;
        boolean urgencyCompleted = true;
        boolean prevAttendCompleted = true;
        boolean otherCompleted = true;
        boolean childProtectionCompleted = true;

        if (exceptions.isPresent() && exceptions.get().contains(domesticViolence)) {
            dvCompleted = anyNonEmpty(caseData.getMiamDomesticViolenceChecklist());
        }
        if (exceptions.isPresent() && exceptions.get().contains(urgency)) {
            urgencyCompleted = anyNonEmpty(caseData.getMiamUrgencyReasonChecklist());
        }
        if (exceptions.isPresent() && exceptions.get().contains(previousMIAMattendance)) {
            prevAttendCompleted = anyNonEmpty(caseData.getMiamPreviousAttendanceChecklist());
        }
        if (exceptions.isPresent() && exceptions.get().contains(other)) {
            otherCompleted = anyNonEmpty(caseData.getMiamOtherGroundsChecklist());
        }
        if (exceptions.isPresent() && exceptions.get().contains(childProtectionConcern)) {
            childProtectionCompleted = anyNonEmpty(caseData.getMiamChildProtectionConcernList());
        }

        return dvCompleted && urgencyCompleted && prevAttendCompleted && otherCompleted && childProtectionCompleted;

    }

}

