package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseClosedDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.CaseClosedDateGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CaseClosedDateGeneratorTest {

    private final CaseClosedDateGenerator generator = new CaseClosedDateGenerator();

    @Test
    public void testGenerate() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                .finalCaseClosedDate("24 Sep 2024")
                .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                .caseClosedDate(CaseClosedDate.builder().closedDate("24 Sep 2024")
                        .build())
                .build());

    }
}
