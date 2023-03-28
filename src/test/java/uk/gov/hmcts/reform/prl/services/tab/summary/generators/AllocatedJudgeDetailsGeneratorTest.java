package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.AllocatedJudgeDetailsGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AllocatedJudgeDetailsGeneratorTest {

    private final AllocatedJudgeDetailsGenerator generator = new AllocatedJudgeDetailsGenerator();

    @Test
    public void testGenerate() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .courtName("Test Court")
                                                             .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .allocatedJudgeDetails(AllocatedJudge.builder()
                                                      .judgeTitle(" ").emailAddress(" ").lastName(" ")
                                                      .courtName("Test Court")
                                                                         .build())
                                                               .build());

    }
}
