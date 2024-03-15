package uk.gov.hmcts.reform.prl.services.tab.alltabs;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

public interface AllTabsService {
    StartAllTabsUpdateDataContent getStartAllTabsUpdate(String caseId);

    StartAllTabsUpdateDataContent getStartUpdateForSpecificEvent(String caseId, String eventId);

    CaseDetails updateAllTabsIncludingConfTab(String caseId);

    Map<String, Object> getAllTabsFields(CaseData caseData);

    StartAllTabsUpdateDataContent getStartUpdateForSpecificUserEvent(String caseId, String eventId, String authorization, boolean isRepresented);
}
