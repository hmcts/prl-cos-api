package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AmendDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class RemoveDraftOrderServiceTest {

    public static final String authToken = "Bearer TestAuthToken";

    @InjectMocks
    private RemoveDraftOrderService removeDraftOrderService;

    @Mock
    private UserService userService;

    @Mock
    ElementUtils elementUtils;

    private static final String testAuth = "auth";

    private static final String REMOVE_DRAFT_ORDERS_DYNAMIC_LIST = "removeDraftOrdersDynamicList";
    private static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";

    @Before
    public void setUp() {
        Mockito.when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder()
                                                                                     .email("test@gmail.com")
                                                                                     .build());
        Mockito.when(elementUtils.getDynamicListSelectedValue(Mockito.any(),Mockito.any()))
            .thenReturn(UUID.fromString(PrlAppsConstants.TEST_UUID));
    }

    @Test
    public void testReturnedOrderDynamicList() {
        List<Element<DraftOrder>> draftOrderCollection = List.of(Element.<DraftOrder>builder().value(DraftOrder.builder().otherDetails(
            OtherDraftOrderDetails.builder()
                .status(OrderStatusEnum.rejectedByJudge.getDisplayedValue())
                .orderCreatedByEmailId("test@gmail.com")
                .build()).build()).build(), Element.<DraftOrder>builder().value(DraftOrder.builder().otherDetails(
            OtherDraftOrderDetails.builder()
                .status(OrderStatusEnum.draftedByLR.getDisplayedValue())
                .orderCreatedByEmailId("test1@gmail.com")
                .build()).build()).build());

        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        Map<String, Object> returnMap = removeDraftOrderService.getDraftOrderDynamicList(caseData, Event.REMOVE_DRAFT_ORDER.getId(), testAuth);
        assertNotNull(returnMap);
        DynamicList supportedDraftOrderList = (DynamicList) returnMap.get(REMOVE_DRAFT_ORDERS_DYNAMIC_LIST);
        assertEquals(0, supportedDraftOrderList.getListItems().size());
        assertEquals(C100_CASE_TYPE, returnMap.get(CASE_TYPE_OF_APPLICATION));
    }

    @Test
    public void testReturnedOrderDynamicListWithWorngEventId() {
        List<Element<DraftOrder>> draftOrderCollection = List.of(Element.<DraftOrder>builder().value(DraftOrder.builder().otherDetails(
            OtherDraftOrderDetails.builder()
                .status(OrderStatusEnum.rejectedByJudge.getDisplayedValue())
                .orderCreatedByEmailId("test@gmail.com")
                .build()).build()).build(), Element.<DraftOrder>builder().value(DraftOrder.builder().otherDetails(
            OtherDraftOrderDetails.builder()
                .status(OrderStatusEnum.draftedByLR.getDisplayedValue())
                .orderCreatedByEmailId("test1@gmail.com")
                .build()).build()).build());

        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        Map<String, Object> returnMap = removeDraftOrderService.getDraftOrderDynamicList(caseData,
                                                                                         Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId(),
                                                                                         testAuth);
        assertNotNull(returnMap);
        assertNotNull(returnMap.get(REMOVE_DRAFT_ORDERS_DYNAMIC_LIST));
        DynamicList supportedDraftOrderList = (DynamicList) returnMap.get(REMOVE_DRAFT_ORDERS_DYNAMIC_LIST);
        assertEquals(0, supportedDraftOrderList.getListItems().size());
        assertEquals(C100_CASE_TYPE, returnMap.get(CASE_TYPE_OF_APPLICATION));
    }

    @Test
    public void testRemoveSelectedDraftOrder() {

        UUID id = UUID.randomUUID();
        List<Element<DraftOrder>> draftOrderCollection = List.of(Element.<DraftOrder>builder()
             .id(UUID.fromString(PrlAppsConstants.TEST_UUID))
             .value(DraftOrder.builder().otherDetails(
                OtherDraftOrderDetails.builder()
                    .status(OrderStatusEnum.rejectedByJudge.getDisplayedValue())
                    .orderCreatedByEmailId("test@gmail.com")
                    .build()).build()).build(),
                Element.<DraftOrder>builder()
                    .id(id)
                    .value(DraftOrder.builder().otherDetails(
                    OtherDraftOrderDetails.builder()
                        .status(OrderStatusEnum.draftedByLR.getDisplayedValue())
                        .orderCreatedByEmailId("test1@gmail.com")
                        .build()).build()).build());

        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code(UUID.fromString(PrlAppsConstants.TEST_UUID))
            .label(" ").build();
        DynamicList dynamicList = DynamicList.builder()
            .listItems(List.of(dynamicListElement))
            .value(dynamicListElement)
            .build();

        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .draftOrderCollection(draftOrderCollection)
            .amendDraftOrderDetails(AmendDraftOrderDetails.builder()
                                        .removeDraftOrdersDynamicList(dynamicList)
                                        .build())

            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        List<Element<DraftOrder>> returnDraftOrder = removeDraftOrderService.removeSelectedDraftOrder(caseData);
        assertNotNull(returnDraftOrder);
        Element<DraftOrder> element = (Element<DraftOrder>) returnDraftOrder.get(0);
        assertEquals(id, element.getId());
        assertNotNull(element.getValue());
    }

    @Test
    public void testRemoveSelectedDraftOrderRemoveDraftOrderDynamicListNull() {

        UUID id = UUID.randomUUID();
        List<Element<DraftOrder>> draftOrderCollection = List.of(Element.<DraftOrder>builder()
            .id(UUID.fromString(PrlAppsConstants.TEST_UUID))
            .value(DraftOrder.builder().otherDetails(
             OtherDraftOrderDetails.builder()
                 .status(OrderStatusEnum.rejectedByJudge.getDisplayedValue())
                 .orderCreatedByEmailId("test@gmail.com")
                 .build()).build()).build(),
            Element.<DraftOrder>builder()
            .id(id)
            .value(DraftOrder.builder().otherDetails(
             OtherDraftOrderDetails.builder()
                 .status(OrderStatusEnum.draftedByLR.getDisplayedValue())
                 .orderCreatedByEmailId("test1@gmail.com")
                 .build()).build()).build());

        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .draftOrderCollection(draftOrderCollection)
            .amendDraftOrderDetails(AmendDraftOrderDetails.builder()
                                        .build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        List<Element<DraftOrder>> returnDraftOrder = removeDraftOrderService.removeSelectedDraftOrder(caseData);
        assertNotNull(returnDraftOrder);
        assertEquals(2, returnDraftOrder.size());
        Element<DraftOrder> element = (Element<DraftOrder>) returnDraftOrder.get(0);
        assertEquals(UUID.fromString(PrlAppsConstants.TEST_UUID), ((Element<DraftOrder>) returnDraftOrder.get(0)).getId());
        assertEquals(id, ((Element<DraftOrder>) returnDraftOrder.get(1)).getId());
    }

    @Test
    public void testRemoveSelectedDraftOrderDraftOrderCollectionIsNull() {

        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code(UUID.fromString(PrlAppsConstants.TEST_UUID))
            .label(" ").build();
        DynamicList dynamicList = DynamicList.builder()
            .listItems(List.of(dynamicListElement))
            .value(dynamicListElement)
            .build();

        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .amendDraftOrderDetails(AmendDraftOrderDetails.builder()
                                        .removeDraftOrdersDynamicList(dynamicList)
                                        .build())

            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        List<Element<DraftOrder>> returnDraftOrder = removeDraftOrderService.removeSelectedDraftOrder(caseData);
        assertNull(returnDraftOrder);
    }
}
