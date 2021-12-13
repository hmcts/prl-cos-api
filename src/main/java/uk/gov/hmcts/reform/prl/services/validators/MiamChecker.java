package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.*;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
public class MiamChecker implements EventChecker {
    @Override
    public boolean isFinished(CaseData caseData) {

        //TODO: use optionals in case data
        if (caseData.getApplicantAttendedMiam() != null) {
            if (caseData.getApplicantAttendedMiam().equals(YES)) {
                return allNonEmpty(
                    caseData.getMediatorRegistrationNumber(),
                    caseData.getFamilyMediatorServiceName(),
                    caseData.getSoleTraderName(),
                    caseData.getMiamCertificationDocumentUpload()
                );
            }
            else if (caseData.getApplicantAttendedMiam().equals(NO) &&
                     caseData.getClaimingExemptionMiam().equals(YES) &&
                     caseData.getFamilyMediatorMiam().equals(YES)) {
                return allNonEmpty(
                    caseData.getMediatorRegistrationNumber1(),
                    caseData.getFamilyMediatorServiceName1(),
                    caseData.getSoleTraderName1(),
                    caseData.getMiamCertificationDocumentUpload1()
                );
            }
            else {
                return checkMIAMExemptions(caseData);
            }
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

