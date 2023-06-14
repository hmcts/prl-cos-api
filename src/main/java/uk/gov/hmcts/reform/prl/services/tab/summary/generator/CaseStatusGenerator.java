package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

@Slf4j
@Component
public class CaseStatusGenerator implements FieldGenerator {
    @Override
    public CaseSummary generate(CaseData caseData) {
        log.info("Case state before updating in CaseStatusGenerator {}", caseData.getState());
        CaseSummary build = CaseSummary.builder().caseStatus(CaseStatus.builder()
                                                                 .state(CaseUtils.getStateLabel(caseData.getState()))
                                                                 .build()).build();
        log.info("Case state after updating in CaseStatusGenerator {}", build.getCaseStatus());

        return build;
    }
}
