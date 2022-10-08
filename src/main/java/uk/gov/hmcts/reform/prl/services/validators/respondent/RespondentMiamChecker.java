package uk.gov.hmcts.reform.prl.services.validators.respondent;

import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.validators.EventChecker;

public class RespondentMiamChecker implements EventChecker {

    @Override
    public boolean isFinished(CaseData caseData) {
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }
}
