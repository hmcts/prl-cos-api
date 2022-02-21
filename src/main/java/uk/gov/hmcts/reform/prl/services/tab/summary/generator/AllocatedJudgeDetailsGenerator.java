package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

@Component
public class AllocatedJudgeDetailsGenerator implements FieldGenerator {

    @Override
    public CaseSummary generate(CaseData caseData) {
        return CaseSummary.builder().allocatedJudgeDetails(
            AllocatedJudge.builder().courtName(CommonUtils.getValue(caseData.getCourtName()))
                .emailAddress(" ").judgeTitle(" ").lastName(" ").build()).build();
    }
}
