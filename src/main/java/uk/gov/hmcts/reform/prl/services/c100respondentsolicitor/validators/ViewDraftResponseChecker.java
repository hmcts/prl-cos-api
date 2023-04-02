package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@Service
public class ViewDraftResponseChecker implements RespondentEventChecker {
    @Override
    public boolean isFinished(CaseData caseData, String respondent) {
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData, String respondent) {
        return false;
    }
}
