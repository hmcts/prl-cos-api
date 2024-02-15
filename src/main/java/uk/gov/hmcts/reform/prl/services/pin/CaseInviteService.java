package uk.gov.hmcts.reform.prl.services.pin;

import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

public interface CaseInviteService {

    public Map<String, Object> generateAndSendCaseInvite(CaseData caseData);


}
