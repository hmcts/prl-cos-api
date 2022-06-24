package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OrderAppliedFor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.OrderAppliedForGenerator;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class OrderAppliedForGeneratorTest {

    private final OrderAppliedForGenerator generator = new OrderAppliedForGenerator();

    @Test
    public void testGenerate() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .ordersApplyingFor(
                                                             Arrays.asList(OrderTypeEnum.childArrangementsOrder,
                                                                           OrderTypeEnum.prohibitedStepsOrder)
                                                                 ).typeOfChildArrangementsOrder(
                ChildArrangementOrderTypeEnum.spendTimeWithOrder)
                                                             .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .summaryTabForOrderAppliedFor(OrderAppliedFor.builder()
                                                                                .ordersApplyingFor(
                                                                                    "Child Arrangements Order, "
                                                                                        + "Prohibited Steps Order")
                                                                                .typeOfChildArrangementsOrder(
                                                                                    "Spend time with order")
                                                                                .build())
                                                               .build());

    }

    @Test
    public void testIfOrderAppliedForNotSelected() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .typeOfChildArrangementsOrder(
                ChildArrangementOrderTypeEnum.spendTimeWithOrder)
                                                         .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .summaryTabForOrderAppliedFor(OrderAppliedFor.builder()
                                                                                .build())
                                              .build());

    }
}
