package uk.gov.hmcts.reform.prl.services.tab.alltabs;

import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

public interface AllTabsService {
    public void updateAllTabs(CaseData caseData);

    public Map<String, Object> getAllTabsFields(CaseData caseData);
}
