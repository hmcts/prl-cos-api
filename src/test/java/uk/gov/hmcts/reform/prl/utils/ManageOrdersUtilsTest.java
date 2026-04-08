package uk.gov.hmcts.reform.prl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

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

    // ========== Tests for validateCustomOrderHearingDetails ==========

    @Test
    void testValidateCustomOrderHearingDetails_dateToBeFixed_noHearingType_returnsError() {
        HearingData hearingData = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .hearingTypes(null)
            .build();

        List<Element<HearingData>> ordersHearingDetails = List.of(element(hearingData));

        List<String> errors = ManageOrdersUtils.validateCustomOrderHearingDetails(ordersHearingDetails);

        assertEquals(1, errors.size());
        assertEquals("You must select a hearing type", errors.get(0));
    }

    @Test
    void testValidateCustomOrderHearingDetails_dateConfirmedByListingTeam_noHearingType_returnsError() {
        HearingData hearingData = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedByListingTeam)
            .hearingTypes(null)
            .build();

        List<Element<HearingData>> ordersHearingDetails = List.of(element(hearingData));

        List<String> errors = ManageOrdersUtils.validateCustomOrderHearingDetails(ordersHearingDetails);

        assertEquals(1, errors.size());
        assertEquals("You must select a hearing type", errors.get(0));
    }

    @Test
    void testValidateCustomOrderHearingDetails_dateToBeFixed_withHearingType_noError() {
        DynamicList hearingTypes = DynamicList.builder()
            .value(DynamicListElement.builder().code("ABA5-FOF").label("Finding of Fact").build())
            .build();

        HearingData hearingData = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .hearingTypes(hearingTypes)
            .build();

        List<Element<HearingData>> ordersHearingDetails = List.of(element(hearingData));

        List<String> errors = ManageOrdersUtils.validateCustomOrderHearingDetails(ordersHearingDetails);

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateCustomOrderHearingDetails_dateConfirmedInHearingsTab_noHearingType_noError() {
        // dateConfirmedInHearingsTab is NOT AHR-eligible, so no validation needed
        HearingData hearingData = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .hearingTypes(null)
            .build();

        List<Element<HearingData>> ordersHearingDetails = List.of(element(hearingData));

        List<String> errors = ManageOrdersUtils.validateCustomOrderHearingDetails(ordersHearingDetails);

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateCustomOrderHearingDetails_nullList_noError() {
        List<String> errors = ManageOrdersUtils.validateCustomOrderHearingDetails(null);

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateCustomOrderHearingDetails_emptyList_noError() {
        List<String> errors = ManageOrdersUtils.validateCustomOrderHearingDetails(Collections.emptyList());

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateCustomOrderHearingDetails_hearingTypesWithNullValue_returnsError() {
        // hearingTypes exists but getValue() is null
        DynamicList hearingTypes = DynamicList.builder()
            .value(null)
            .build();

        HearingData hearingData = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .hearingTypes(hearingTypes)
            .build();

        List<Element<HearingData>> ordersHearingDetails = List.of(element(hearingData));

        List<String> errors = ManageOrdersUtils.validateCustomOrderHearingDetails(ordersHearingDetails);

        assertEquals(1, errors.size());
        assertEquals("You must select a hearing type", errors.get(0));
    }
}
