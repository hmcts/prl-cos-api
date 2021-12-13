package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
public class ApplicationTypeChecker implements EventChecker {

    public boolean isStarted(CaseData caseData) {
        return anyNonEmpty(
            caseData.getOrdersApplyingFor(),
            caseData.getTypeOfChildArrangementsOrder(),
            caseData.getNatureOfOrder(),
            caseData.getConsentOrder(),
            caseData.getDraftConsentOrderFile(),
            caseData.getApplicationPermissionRequired(),
            caseData.getApplicationPermissionRequiredReason(),
            caseData.getApplicationDetails()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    @Override
    public boolean isFinished(CaseData caseData) {

        return allNonEmpty(
            caseData.getOrdersApplyingFor(),
            caseData.getNatureOfOrder(),
            caseData.getConsentOrder(),
            caseData.getApplicationPermissionRequired(),
            caseData.getApplicationDetails()
        );

    }
}
