package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.ApplicationTypeOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.TypeOfApplicationGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TypeOfApplicationGeneratorTest {

    private final TypeOfApplicationGenerator generator = new TypeOfApplicationGenerator();


    @Test
    public void testIfTypeOfApplicationIsOneOrMore() {

        List<FL401OrderTypeEnum> enumList = new ArrayList<>();

        enumList.add(FL401OrderTypeEnum.occupationOrder);
        enumList.add(FL401OrderTypeEnum.nonMolestationOrder);

        List<String> orderLists = Arrays.asList("Occupation order", "Non-molestation order");
        TypeOfApplicationOrders orders = TypeOfApplicationOrders.builder().orderType(enumList).build();

        CaseSummary caseSummary = generator.generate(CaseData.builder().typeOfApplicationOrders(orders).build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .applicationTypeOrders(ApplicationTypeOrders.builder().applicationTypeOrders(
                                                      orderLists).build()).build());


    }

}
