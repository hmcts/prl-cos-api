package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OrderAppliedFor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Component
public class OrderAppliedForGenerator implements FieldGenerator {
    @Override
    public CaseSummary generate(CaseData caseData) {
        return CaseSummary.builder().summaryTabForOrderAppliedFor(getOrderAppliedFor(caseData)).build();
    }

    private OrderAppliedFor getOrderAppliedFor(CaseData caseData) {
        Optional<List<OrderTypeEnum>> checkOrders = ofNullable(caseData.getOrdersApplyingFor());
        if (checkOrders.isEmpty()) {
            return OrderAppliedFor.builder().build();
        }
        List<String> ordersApplyingFor = caseData.getOrdersApplyingFor().stream()
            .map(OrderTypeEnum::getDisplayedValue)
            .toList();

        String typeOfChildArrangementsOrder = "";
        Optional<ChildArrangementOrderTypeEnum> childArrangementCheck = ofNullable(caseData.getTypeOfChildArrangementsOrder());
        if (childArrangementCheck.isPresent()) {
            typeOfChildArrangementsOrder = caseData.getTypeOfChildArrangementsOrder().getDisplayedValue();
        }

        return OrderAppliedFor.builder()
            .ordersApplyingFor(String.join(", ", ordersApplyingFor))
            .typeOfChildArrangementsOrder(typeOfChildArrangementsOrder)
            .build();
    }
}
