package uk.gov.hmcts.reform.prl.services.tab.alltabs;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

public interface AllTabsService {
    StartAllTabsUpdateDataContent getStartAllTabsUpdate(String caseId);

    StartAllTabsUpdateDataContent getStartUpdateForSpecificEvent(String caseId, String eventId);

    CaseDetails updateAllTabsIncludingConfTab(String caseId);

    Map<String, Object> getAllTabsFields(CaseData caseData);

    StartAllTabsUpdateDataContent getStartUpdateForSpecificUserEvent(String caseId,
                                                                            String eventId,
                                                                            String authorisation);

    CaseDetails submitUpdateForSpecificUserEvent(String authorisation,
                                                 String caseId,
                                                 StartEventResponse startEventResponse,
                                                 EventRequestData eventRequestData,
                                                 Map<String, Object> combinedFieldsMap,
                                                 UserDetails userDetails);
}
