package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
public class TypeOfApplicationMapperTest {

    @InjectMocks
    TypeOfApplicationMapper typeOfApplicationMapper;


    @Test
    public void testForTypeOfApplicationMapper() {
        List<OrderTypeEnum> orderType = new ArrayList<>();
        orderType.add(OrderTypeEnum.childArrangementsOrder);
        orderType.add(OrderTypeEnum.prohibitedStepsOrder);
        CaseData caseDataInput = CaseData.builder().ordersApplyingFor(orderType).typeOfChildArrangementsOrder(
            ChildArrangementOrderTypeEnum.liveWithOrder).natureOfOrder("test")
            .consentOrder(Yes).applicationPermissionRequired(
            PermissionRequiredEnum.yes).applicationPermissionRequiredReason("Need Permission")
            .applicationDetails("Done").build();
        assertNotNull(typeOfApplicationMapper.map(caseDataInput));
    }

    @Test
    public void testForNoDataTypeOfApplicationMapper() {
        CaseData caseDataInput = CaseData.builder().build();
        assertNotNull(typeOfApplicationMapper.map(caseDataInput));
    }
}
