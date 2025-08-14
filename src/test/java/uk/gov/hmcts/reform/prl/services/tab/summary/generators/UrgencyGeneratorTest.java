package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.Urgency;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.UrgencyGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UrgencyGeneratorTest {

    private final UrgencyGenerator generator = new UrgencyGenerator();

    @Test
    public void testAllUrgencySelected() {
        CaseSummary caseSummary = generator.generate(CaseData.builder().caseTypeOfApplication("C100")
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
        CaseSummary caseSummary = generator.generate(CaseData.builder().caseTypeOfApplication("C100")
                                                         .isCaseUrgent(YesOrNo.No)
                                                         .doYouNeedAWithoutNoticeHearing(YesOrNo.No)
                                                         .doYouRequireAHearingWithReducedNotice(YesOrNo.No)
                                                         .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .urgencyDetails(Urgency.builder().urgencyStatus(
                                                  "Not urgent").build())
                                              .build());

    }

    @Test
    public void testFlUrgencySelected() {
        WithoutNoticeOrderDetails withoutNoticeOrderDetails = new WithoutNoticeOrderDetails(YesOrNo.Yes);
        CaseSummary caseSummary = generator.generate(CaseData.builder().caseTypeOfApplication("FL401").orderWithoutGivingNoticeToRespondent(
            withoutNoticeOrderDetails).build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .urgencyDetails(Urgency.builder().urgencyStatus(
                                                  PrlAppsConstants.WITHOUT_NOTICE).build())
                                              .build());

    }


    @Test
    public void testFlUrgencyNotSelected() {
        WithoutNoticeOrderDetails withoutNoticeOrderDetails = new WithoutNoticeOrderDetails(YesOrNo.No);
        CaseSummary caseSummary = generator.generate(CaseData.builder().caseTypeOfApplication("FL401").orderWithoutGivingNoticeToRespondent(
            withoutNoticeOrderDetails).build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .urgencyDetails(Urgency.builder().urgencyStatus(
                                                  PrlAppsConstants.WITH_NOTICE).build())
                                              .build());

    }


}
