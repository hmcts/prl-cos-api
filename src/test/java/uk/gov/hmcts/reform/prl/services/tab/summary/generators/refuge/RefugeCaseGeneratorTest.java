package uk.gov.hmcts.reform.prl.services.tab.summary.generators.refuge;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.refuge.RefugeCase;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.refuge.RefugeCaseGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RefugeCaseGeneratorTest {

    private final RefugeCaseGenerator generator = new RefugeCaseGenerator();

    @Test
    public void testGenerateForC100() {

        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .caseTypeOfApplication("C100")
                                                         .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder().refugeCase(RefugeCase
                                                                               .builder()
                                                                               .isRefugeCase(YesOrNo.No)
                                                                               .build())
                                              .build());
    }

    @Test
    public void testGenerateForFL401() {

        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .caseTypeOfApplication("FL401")
                                                         .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder().refugeCase(RefugeCase
                                                                               .builder()
                                                                               .isRefugeCase(YesOrNo.No)
                                                                               .build())
                                              .build());
    }

}
