package uk.gov.hmcts.reform.prl.repositories;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

public interface CaseRepository {

    void linkDefendant(String authorisation, String anonymousUserToken, String caseId, CaseData caseData);

    CaseDetails updateCase(String authorisation, String caseId, CaseData caseData, CaseEvent caseEvent);

    CaseDetails createCase(String authorisation, CaseData caseData);

}

