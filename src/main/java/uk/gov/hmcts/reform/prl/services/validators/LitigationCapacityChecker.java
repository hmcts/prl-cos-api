package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;

@Service
public class LitigationCapacityChecker implements EventChecker {
    @Override
    public boolean isFinished(CaseData caseData) {

        boolean litigationFactorsComplete = !(caseData.getLitigationCapacityFactors() == null);
        boolean litigationReferralsComplete = !(caseData.getLitigationCapacityReferrals() == null);
        boolean litigationOtherComplete = !(caseData.getLitigationCapacityOtherFactors() == null);
        boolean litigationOtherDetailsComplete = !(caseData.getLitigationCapacityOtherFactorsDetails() == null);

        if (litigationOtherComplete) {
            return  litigationOtherDetailsComplete;
        }
        return litigationFactorsComplete || litigationReferralsComplete;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        YesOrNo otherFactors = caseData.getLitigationCapacityOtherFactors();
        if (otherFactors != null && otherFactors.equals(YES)) {
            return caseData.getLitigationCapacityOtherFactorsDetails() == null;
        }
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }
}
