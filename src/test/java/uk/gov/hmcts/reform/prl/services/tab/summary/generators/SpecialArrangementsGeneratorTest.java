package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.SpecialArrangements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.SpecialArrangementsGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SpecialArrangementsGeneratorTest {

    private final SpecialArrangementsGenerator generator = new SpecialArrangementsGenerator();

    @Test
    public void testIfSpecialArrangementHasMarkedAsYes() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .attendHearing(AttendHearing.builder()
                                                                            .isSpecialArrangementsRequired(YesOrNo.Yes)
                                                                            .build())
                                                             .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .specialArrangement(
                                                  SpecialArrangements.builder()
                                                      .areAnySpecialArrangements(YesOrNo.Yes.getDisplayedValue())
                                                      .build())
                                                               .build());

    }

    @Test
    public void testIfSpecialArrangementHasMarkedAsNo() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .attendHearing(AttendHearing.builder()
                                                                            .isSpecialArrangementsRequired(YesOrNo.No)
                                                                            .build())
                                                         .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .specialArrangement(
                                                  SpecialArrangements.builder()
                                                      .areAnySpecialArrangements(YesOrNo.No.getDisplayedValue())
                                                      .build())
                                              .build());

    }
}
