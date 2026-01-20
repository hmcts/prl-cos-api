package uk.gov.hmcts.reform.prl.services.highcourt;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Service
public class HighCourtService {

    public void setCaseAccess(CaseDetails caseDetails) {
        if (isHighCourtCase(caseDetails)) {
            addJudiciaryAccess();
        } else {
            revokeJudiciaryAccess();
        }
    }

    private void addJudiciaryAccess() {
        // implement
    }

    private void revokeJudiciaryAccess() {
        // implement
    }

    private boolean isHighCourtCase(CaseDetails caseDetails) {
        Object highCourtCase = caseDetails.getData().get("isHighCourtCase");
        return highCourtCase.equals(YesOrNo.Yes);
    }
}
