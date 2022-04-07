package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.ApplicationTypeOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Component
public class TypeOfApplicationGenerator implements FieldGenerator {


    @Override
    public CaseSummary generate(CaseData caseData) {

        return CaseSummary.builder().applicationTypeOrders(ApplicationTypeOrders.builder().applicationTypeOrders(
            getTypeOfApplication(caseData)).build()).build();
    }

    private List<String> getTypeOfApplication(CaseData caseData) {

        List<String> displayedValues = new ArrayList<>();
        Optional<TypeOfApplicationOrders> applicationOrder = ofNullable(caseData.getTypeOfApplicationOrders());
        if (applicationOrder.isPresent() && !applicationOrder.get().getOrderType().isEmpty()) {
            displayedValues = applicationOrder.get().getOrderType().stream().map(FL401OrderTypeEnum::getDisplayedValue).collect(
                Collectors.toList());
        }
        return displayedValues;
    }
}
