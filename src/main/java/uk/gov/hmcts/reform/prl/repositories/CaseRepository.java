package uk.gov.hmcts.reform.prl.repositories;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

public interface CaseRepository {

    void linkDefendant(String userAuthorisation, String systemUserId, String systemUserToken, String caseId,
                       EventRequestData eventRequestData, StartEventResponse startEventResponse, Map<String, Object> caseDataUpdated);

    CaseDetails updateCase(String authorisation, String caseId, CaseData caseData, CaseEvent caseEvent);

    CaseDetails createCase(String authorisation, CaseData caseData);

    CaseDetails getCase(String authorisation, String caseId);

}

