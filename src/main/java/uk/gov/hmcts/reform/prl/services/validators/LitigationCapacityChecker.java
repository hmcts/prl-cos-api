package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static uk.gov.hmcts.reform.prl.enums.Event.LITIGATION_CAPACITY;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;

@Service
public class LitigationCapacityChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

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
            if(caseData.getLitigationCapacityOtherFactorsDetails() == null) {
                taskErrorService.addEventError(LITIGATION_CAPACITY, "Add details of other factors");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }
}
