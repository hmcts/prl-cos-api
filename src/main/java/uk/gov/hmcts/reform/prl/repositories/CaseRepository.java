package uk.gov.hmcts.reform.prl.repositories;

import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

public interface CaseRepository {

    void linkDefendant(String authorisation, String anonymousUserToken, String s2sToken, String caseId, CaseData caseData);

}

