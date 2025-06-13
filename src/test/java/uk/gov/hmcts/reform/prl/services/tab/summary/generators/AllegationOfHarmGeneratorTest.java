package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.AllegationOfHarmGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AllegationOfHarmGeneratorTest {

    private final AllegationOfHarmGenerator generator = new AllegationOfHarmGenerator();

    @Test
    void testGenerate() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .allegationOfHarm(uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm.builder()
                                                                               .allegationsOfHarmDomesticAbuseYesNo(YesOrNo.Yes)
                                                                               .allegationsOfHarmChildAbductionYesNo(YesOrNo.Yes)
                                                                               .allegationsOfHarmChildAbuseYesNo(YesOrNo.Yes)
                                                                               .allegationsOfHarmOtherConcernsYesNo(YesOrNo.Yes)
                                                                               .allegationsOfHarmSubstanceAbuseYesNo(YesOrNo.Yes)
                                                                               .build())
                                                             .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                         .allegationOfHarm(AllegationOfHarm.builder()
                                                               .typesOfHarm("Domestic abuse, child abduction, "
                                                                                + "child abuse, drugs,"
                                                                                + " alcohol or substance abuse, safety "
                                                                                + "or welfare concerns")
                                                               .build())
                                         .build());
    }

    @Test
    void testNoTypeOfHarm() {
        CaseSummary caseSummary = generator
            .generate(CaseData.builder()
                          .allegationOfHarm(uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm
                                                .builder().build()).build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .allegationOfHarm(AllegationOfHarm.builder()
                                                                    .typesOfHarm("No Allegations of harm")
                                                                    .build())
                                              .build());
    }
}
