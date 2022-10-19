package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.ApplicationTypeDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Component
public class TypeOfApplicationGenerator implements FieldGenerator {


    @Override
    public CaseSummary generate(CaseData caseData) {

        return CaseSummary.builder().applicationTypeDetails(ApplicationTypeDetails.builder()
                                                                .typesOfApplication(getTypeOfApplication(caseData)).build()).build();

    }

    private String getTypeOfApplication(CaseData caseData) {

        String displayedValues = PrlAppsConstants.BLANK_STRING;
        Optional<TypeOfApplicationOrders> applicationOrder = ofNullable(caseData.getTypeOfApplicationOrders());
        if (applicationOrder.isPresent()) {
            Optional<List<FL401OrderTypeEnum>> orderTypeEnums = ofNullable(applicationOrder.get().getOrderType());
            if (orderTypeEnums.isPresent()) {
                displayedValues = orderTypeEnums.get().stream().map(FL401OrderTypeEnum::getDisplayedValue)
                    .collect(Collectors.joining(System.lineSeparator()));
            }
        }
        return displayedValues;
    }

}

