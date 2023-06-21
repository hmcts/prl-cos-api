package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.CaseStatusGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CaseStatusGeneratorTest {

    private final CaseStatusGenerator generator = new CaseStatusGenerator();

    @Test
    public void testGenerate() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .state(State.AWAITING_RESUBMISSION_TO_HMCTS)
                                                             .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .caseStatus(CaseStatus.builder().state(
                                                  State.AWAITING_RESUBMISSION_TO_HMCTS.getLabel()).build())
                                                               .build());

    }
}
