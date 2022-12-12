package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.AllegationOfHarmRevisedGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AllegationOfHarmRevisedGeneratorTest {

    private final AllegationOfHarmRevisedGenerator generator = new AllegationOfHarmRevisedGenerator();

    @Test
    public void testGenerate() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                    .allegationOfHarmRevised(
                                        uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised.builder()
                                                                               .newAllegationsOfHarmDomesticAbuseYesNo(YesOrNo.Yes)
                                                                               .newAllegationsOfHarmChildAbductionYesNo(YesOrNo.Yes)
                                                                               .newAllegationsOfHarmChildAbuseYesNo(YesOrNo.Yes)
                                                                               .newAllegationsOfHarmOtherConcerns(YesOrNo.Yes)
                                                                               .newAllegationsOfHarmSubstanceAbuseYesNo(YesOrNo.Yes)
                                                                               .build())
                                                             .build());
        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                         .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                              .typesOfHarmRevised("Domestic abuse, child abduction, "
                                                                                + "child abuse, drugs,"
                                                                                + " alcohol or substance abuse, safety "
                                                                                + "or welfare concerns")
                                                               .build())
                                         .build());
    }


    @Test
    public void testNoTypeOfHarm() {
        CaseSummary caseSummary = generator
            .generate(CaseData.builder()
                          .allegationOfHarmRevised(uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised
                                                .builder().build()).build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                                                           .typesOfHarmRevised("No Allegations of harm")
                                                                    .build())
                                              .build());
    }
}
