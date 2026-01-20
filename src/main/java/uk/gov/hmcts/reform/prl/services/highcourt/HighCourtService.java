package uk.gov.hmcts.reform.prl.services.highcourt;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Service
public class HighCourtService {

    public void setCaseAccess(CaseDetails caseDetails) {
        if (isHighCourtCase(caseDetails)) {
            grantJudiciaryAccess();
        } else {
            revokeJudiciaryAccess();
        }
    }

    private void grantJudiciaryAccess() {
        // implement
    }

    private void revokeJudiciaryAccess() {
        // implement
        // removeRoleAssignment("")
    }

    private boolean isHighCourtCase(CaseDetails caseDetails) {
        Object highCourtCase = caseDetails.getData().get("isHighCourtCase");
        return highCourtCase.equals(YesOrNo.Yes);
    }
}
