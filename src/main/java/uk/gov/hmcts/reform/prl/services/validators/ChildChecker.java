package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@Service
public class ChildChecker implements EventChecker{
    @Override
    public boolean isFinished(CaseData caseData) {
        return caseData.getChildren() != null;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return caseData.getChildren() != null;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }
}
