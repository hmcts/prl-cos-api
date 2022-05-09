package uk.gov.hmcts.reform.prl.services.pin;

import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

public interface CaseInviteService {

    public CaseData generateAndSendRespondentCaseInvite(CaseData caseData);


}
