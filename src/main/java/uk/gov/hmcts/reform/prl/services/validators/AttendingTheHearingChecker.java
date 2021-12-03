package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
public class AttendingTheHearingChecker implements EventChecker {

    @Override
    public boolean isFinished(CaseData caseData) {
        return allNonEmpty(
            caseData.getIsWelshNeeded(),
            caseData.getIsInterpreterNeeded(),
            caseData.getIsDisabilityPresent(),
            caseData.getIsSpecialArrangementsRequired(),
            caseData.getIsIntermediaryNeeded()
        );
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return anyNonEmpty(
            caseData.getIsWelshNeeded(),
            caseData.getIsInterpreterNeeded(),
            caseData.getIsDisabilityPresent(),
            caseData.getSpecialArrangementsRequired(),
            caseData.getIsIntermediaryNeeded()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }
}
