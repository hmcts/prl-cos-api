package uk.gov.hmcts.reform.prl.services.tab;

import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.FieldGenerator;

import java.util.List;
import java.util.Map;

public interface TabService {
    public Map<String, Object> updateTab(CaseData caseData);

    public List<FieldGenerator> getGenerators();

    public void calEventToRefreshUI();
}
