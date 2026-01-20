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
        // check if an admin assigned a task to a specific high court judge so the judge wouldn't be removed in that case
        // - will this prevent the case from being set to normal or will it be set to normal but that specific judge will have access?
        // removeRoleAssignment("")
    }

    private boolean isHighCourtCase(CaseDetails caseDetails) {
        Object highCourtCase = caseDetails.getData().get("isHighCourtCase");
        return highCourtCase.equals(YesOrNo.Yes);
    }
}
