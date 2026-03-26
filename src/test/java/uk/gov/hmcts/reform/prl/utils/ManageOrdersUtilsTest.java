package uk.gov.hmcts.reform.prl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ManageOrdersUtilsTest {

    @Test
    void testBuildC43OrderName_singleChildArrangementsWithSubType() {
        List<OrderTypeEnum> orders = List.of(OrderTypeEnum.childArrangementsOrder);
        ChildArrangementOrderTypeEnum subType = ChildArrangementOrderTypeEnum.liveWithOrder;

        String result = ManageOrdersUtils.buildC43OrderName(orders, subType);

        assertEquals("Child Arrangements Order (Live with order)", result);
    }

    @Test
    void testBuildC43OrderName_singleProhibitedSteps() {
        List<OrderTypeEnum> orders = List.of(OrderTypeEnum.prohibitedStepsOrder);

        String result = ManageOrdersUtils.buildC43OrderName(orders, null);

        assertEquals("Prohibited Steps Order", result);
    }

    @Test
    void testBuildC43OrderName_multipleOrdersWithChildArrangements() {
        List<OrderTypeEnum> orders = Arrays.asList(
            OrderTypeEnum.childArrangementsOrder,
            OrderTypeEnum.prohibitedStepsOrder,
            OrderTypeEnum.specificIssueOrder
        );
        ChildArrangementOrderTypeEnum subType = ChildArrangementOrderTypeEnum.spendTimeWithOrder;

        String result = ManageOrdersUtils.buildC43OrderName(orders, subType);

        assertEquals("Child Arrangements Order (Spend time with order), Prohibited Steps Order, Specific Issue Order", result);
    }

    @Test
    void testBuildC43OrderName_multipleOrdersWithoutChildArrangements() {
        List<OrderTypeEnum> orders = Arrays.asList(
            OrderTypeEnum.prohibitedStepsOrder,
            OrderTypeEnum.specificIssueOrder
        );

        String result = ManageOrdersUtils.buildC43OrderName(orders, null);

        assertEquals("Prohibited Steps Order, Specific Issue Order", result);
    }

    @Test
    void testBuildC43OrderName_childArrangementsWithBothLiveWithAndSpendTime() {
        List<OrderTypeEnum> orders = List.of(OrderTypeEnum.childArrangementsOrder);
        ChildArrangementOrderTypeEnum subType = ChildArrangementOrderTypeEnum.bothLiveWithAndSpendTimeWithOrder;

        String result = ManageOrdersUtils.buildC43OrderName(orders, subType);

        assertEquals("Child Arrangements Order (Both live with and spend time with order)", result);
    }

    @Test
    void testBuildC43OrderName_childArrangementsWithoutSubType() {
        List<OrderTypeEnum> orders = List.of(OrderTypeEnum.childArrangementsOrder);

        String result = ManageOrdersUtils.buildC43OrderName(orders, null);

        assertEquals("Child Arrangements Order", result);
    }

    @Test
    void testBuildC43OrderName_emptyList() {
        List<OrderTypeEnum> orders = Collections.emptyList();

        String result = ManageOrdersUtils.buildC43OrderName(orders, null);

        assertNull(result);
    }

    @Test
    void testBuildC43OrderName_nullList() {
        String result = ManageOrdersUtils.buildC43OrderName(null, null);

        assertNull(result);
    }
}
