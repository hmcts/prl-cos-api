package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

public interface FieldGenerator {
    public CaseSummary generate(CaseData caseData);
}
