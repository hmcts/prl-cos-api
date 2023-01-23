package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@Service
public class CaseNameChecker implements EventChecker {

    @Override
    public boolean isFinished(CaseData caseData) {

        String caseName = caseData.getApplicantCaseName();
        return caseName != null;
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
