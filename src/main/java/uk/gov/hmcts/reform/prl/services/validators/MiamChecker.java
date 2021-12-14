package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.documents.MiamDocument;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.MIAM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.*;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
public class MiamChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        boolean finished;

        Optional<YesOrNo> applicantAttendedMiam = ofNullable(caseData.getApplicantAttendedMiam());
        Optional<YesOrNo> claimingExemptionMiam = ofNullable(caseData.getClaimingExemptionMiam());
        Optional<YesOrNo> familyMediatiorMiam = ofNullable(caseData.getFamilyMediatorMiam());

        Optional<String> mediatorRegNumber = ofNullable(caseData.getMediatorRegistrationNumber());
        Optional<String> mediatorServiceName = ofNullable(caseData.getFamilyMediatorServiceName());
        Optional<String> mediatorSoleTrader = ofNullable(caseData.getSoleTraderName());
        Optional<MiamDocument> miamCertDocument = ofNullable(caseData.getMiamCertificationDocumentUpload());

        if (applicantAttendedMiam.isPresent() && applicantAttendedMiam.get().equals(YES)) {
            finished = mediatorRegNumber.isPresent() &&
                mediatorServiceName.isPresent() &&
                mediatorSoleTrader.isPresent() &&
                miamCertDocument.isPresent();

            if (finished) {
                taskErrorService.removeError(MIAM_ERROR);
                return true;
            }
        } else if ((applicantAttendedMiam.isPresent() && applicantAttendedMiam.get().equals(NO)) &&
            (claimingExemptionMiam.isPresent() && claimingExemptionMiam.get().equals(YES)) &&
            (familyMediatiorMiam.isPresent() && familyMediatiorMiam.get().equals(YES))) {

            Optional<String> mediatorRegNumber1 = ofNullable(caseData.getMediatorRegistrationNumber1());
            Optional<String> mediatorServiceName1 = ofNullable(caseData.getFamilyMediatorServiceName1());
            Optional<String> mediatorSoleTrader1 = ofNullable(caseData.getSoleTraderName1());
            Optional<MiamDocument> miamCertDocument1 = ofNullable(caseData.getMiamCertificationDocumentUpload1());

            finished = mediatorRegNumber1.isPresent() &&
                mediatorServiceName1.isPresent() &&
                mediatorSoleTrader1.isPresent() &&
                miamCertDocument1.isPresent();

            if (finished) {
                taskErrorService.removeError(MIAM_ERROR);
                return true;
            }
        }
        else {
            return checkMIAMExemptions(caseData);
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

    public boolean checkMIAMExemptions(CaseData caseData) {

        List<MiamExemptionsChecklistEnum> exceptions = caseData.getMiamExemptionsChecklist();

        boolean dvCompleted = true;
        boolean urgencyCompleted = true;
        boolean prevAttendCompleted = true;
        boolean otherCompleted = true;

        if (exceptions.contains(domesticViolence)) {
            dvCompleted = anyNonEmpty(caseData.getMiamDomesticViolenceChecklist());
        }
        if (exceptions.contains(urgency)) {
            urgencyCompleted = anyNonEmpty(caseData.getMiamUrgencyReasonChecklist());
        }
        if (exceptions.contains(previousMIAMattendance)) {
            urgencyCompleted = anyNonEmpty(caseData.getMiamPreviousAttendanceChecklist());
        }
        if (exceptions.contains(other)) {
            urgencyCompleted = anyNonEmpty(caseData.getMiamOtherGroundsChecklist());
        }

        return dvCompleted && urgencyCompleted && prevAttendCompleted && otherCompleted;

    }

}

