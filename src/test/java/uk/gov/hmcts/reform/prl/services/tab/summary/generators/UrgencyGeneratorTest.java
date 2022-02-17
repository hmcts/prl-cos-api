package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.Urgency;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.UrgencyGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UrgencyGeneratorTest {

    private final UrgencyGenerator generator = new UrgencyGenerator();

    @Test
    public void testAllUrgencySelected() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .isCaseUrgent(YesOrNo.Yes)
                                                         .doYouNeedAWithoutNoticeHearing(YesOrNo.Yes)
                                                         .doYouRequireAHearingWithReducedNotice(YesOrNo.Yes)
                                                             .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .urgencyDetails(Urgency.builder().urgencyStatus(
                                                  "Urgent, without notice, reduced notice").build())
                                                               .build());

    }

    @Test
    public void testNoUrgencySelected() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .isCaseUrgent(YesOrNo.No)
                                                         .doYouNeedAWithoutNoticeHearing(YesOrNo.No)
                                                         .doYouRequireAHearingWithReducedNotice(YesOrNo.No)
                                                         .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .urgencyDetails(Urgency.builder().urgencyStatus(
                                                  "Not urgent").build())
                                              .build());

    }
}
