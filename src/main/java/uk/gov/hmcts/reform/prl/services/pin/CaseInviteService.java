package uk.gov.hmcts.reform.prl.services.pin;

import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

public interface CaseInviteService {

    public CaseData sendCaseInviteEmail(CaseData caseData);


}
