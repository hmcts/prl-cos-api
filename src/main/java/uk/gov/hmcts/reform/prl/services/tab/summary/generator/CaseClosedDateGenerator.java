package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseClosedDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@Component
public class CaseClosedDateGenerator implements FieldGenerator {

    @Override
    public CaseSummary generate(CaseData caseData) {
        return CaseSummary.builder().caseClosedDate(CaseClosedDate.builder()
                                                    .closedDate(caseData.getFinalCaseClosedDate())
                                                    .build()).build();
    }
}
