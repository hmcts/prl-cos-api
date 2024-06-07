package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TEST_UUID;


@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class EditReturnedOrderServiceTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String DRAFT_ORDER_COLLECTION = "draftOrderCollection";

    @InjectMocks
    private EditReturnedOrderService editReturnedOrderService;

    @Mock
    private UserService userService;

    @Mock
    ElementUtils elementUtils;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    DraftAnOrderService draftAnOrderService;

    @Mock
    DynamicMultiSelectListService dynamicMultiSelectListService;

    @Mock
    HearingDataService hearingDataService;

    @Mock
    ManageOrderService manageOrderService;

    private static final String testAuth = "auth";

    @Before
    public void setUp() {
        Mockito.when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder()
                                                                                     .email("test@gmail.com")
                                                                                     .build());
        Mockito.when(elementUtils.getDynamicListSelectedValue(Mockito.any(),Mockito.any()))
            .thenReturn(UUID.fromString(PrlAppsConstants.TEST_UUID));
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(),Mockito.any()))
            .thenReturn(DraftOrder.builder()
                            .orderType(CreateSelectOrderOptionsEnum.generalForm)
                            .otherDetails(OtherDraftOrderDetails.builder().instructionsToLegalRepresentative("u").build()).build());
    }


    @Test
    public void testHandleAboutToStart() {
        List<Element<DraftOrder>> draftOrderCollection = List.of(Element.<DraftOrder>builder().value(DraftOrder.builder().otherDetails(
            OtherDraftOrderDetails.builder()
                .status(OrderStatusEnum.rejectedByJudge.getDisplayedValue())
                .orderCreatedByEmailId("test@gmail.com")
                .build()).build()).build());

        List<List<Element<DraftOrder>>> listOfDraftOrderList = new ArrayList<>();
        listOfDraftOrderList.add(draftOrderCollection);
        listOfDraftOrderList.add(null);

        for (List<Element<DraftOrder>> obj : listOfDraftOrderList) {
            CaseData caseData = CaseData.builder()
                .id(123L)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .draftOrderCollection(obj)
                .state(State.CASE_ISSUED)
                .build();
            Map<String, Object> caseDataMap = new HashMap<>();
            when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
            CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                 .id(123L)
                                 .data(caseDataMap)
                                 .build())
                .build();
            assertNotNull(editReturnedOrderService.handleAboutToStartCallback(testAuth, callbackRequest));
        }
    }

    @Test
    public void testHandleAboutToStartWithoutDraftOrderCollection() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .build();
        assertNotNull(editReturnedOrderService.handleAboutToStartCallback(testAuth, callbackRequest).getErrors());
    }

    @Test
    public void testReturnedOrderDynamicList() {
        List<Element<DraftOrder>> draftOrderCollection1 = List.of(Element.<DraftOrder>builder().value(DraftOrder.builder().otherDetails(
            OtherDraftOrderDetails.builder()
                .status(OrderStatusEnum.rejectedByJudge.getDisplayedValue())
                .orderCreatedByEmailId("test@gmail.com")
                .build()).build()).build());
        List<Element<DraftOrder>> draftOrderCollection2 = List.of(Element.<DraftOrder>builder().value(DraftOrder.builder().otherDetails(
            OtherDraftOrderDetails.builder()
                .status(OrderStatusEnum.draftedByLR.getDisplayedValue())
                .orderCreatedByEmailId("test1@gmail.com")
                .build()).build()).build());

        List<List<Element<DraftOrder>>> listOfDraftOrderList = new ArrayList<>();
        listOfDraftOrderList.add(draftOrderCollection1);
        listOfDraftOrderList.add(draftOrderCollection2);

        for (List<Element<DraftOrder>> obj : listOfDraftOrderList) {
            CaseData caseData = CaseData.builder()
                .draftOrderCollection(obj).build();
            assertNotNull(editReturnedOrderService.getReturnedOrdersDynamicList(testAuth, caseData));
        }
    }

    @Test
    public void testInstructionToLegalRepresentative() {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .rejectedOrdersDynamicList(DynamicList.builder()
                                                             .value(DynamicListElement.builder().code(PrlAppsConstants.TEST_UUID)
                                                                        .build())
                                                             .build()).build())
            .build();

        Map<String, Object> response = editReturnedOrderService
            .populateInstructionsAndDocuments(caseData, DraftOrder.builder()
                .otherDetails(OtherDraftOrderDetails.builder().instructionsToLegalRepresentative("hello").build())
                .build());
        assertTrue(response.containsKey("instructionsToLegalRepresentative"));
    }

    @Test
    public void testInstructionToLegalRepresentativeElseCondition() {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .rejectedOrdersDynamicList(DynamicList.builder()
                                                             .value(DynamicListElement.builder().code(PrlAppsConstants.TEST_UUID)
                                                                        .build())
                                                             .build()).build())
            .build();
        Map<String, Object> response = editReturnedOrderService
            .populateInstructionsAndDocuments(caseData, DraftOrder.builder()
                .otherDetails(OtherDraftOrderDetails.builder().build())
            .build());
        assertTrue(response.containsKey("editOrderTextInstructions"));
    }

    @Test
    public void  testInstructionToLegalRepresentativeWithJudgeInstructions() {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .rejectedOrdersDynamicList(DynamicList.builder()
                                                             .value(DynamicListElement.builder().code(PrlAppsConstants.TEST_UUID)
                                                                        .build())
                                                             .build()).build())
            .build();

        Map<String, Object> response = editReturnedOrderService
            .populateInstructionsAndDocuments(caseData, DraftOrder.builder()
                .orderType(CreateSelectOrderOptionsEnum.generalForm)
                .isOrderUploadedByJudgeOrAdmin(YesOrNo.Yes)
                .orderSelectionType(ManageOrdersOptionsEnum.uploadAnOrder.toString())
                .otherDetails(OtherDraftOrderDetails.builder().instructionsToLegalRepresentative("u").build()).build());
        assertTrue(response.containsKey("instructionsToLegalRepresentative"));
    }

    @Ignore
    @Test
    public void  testAboutToSubmitHandlerForDraftedOrder() {
        Map<String, Object> caseDataMap = new HashMap<>();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        DraftOrder draftOrder = DraftOrder.builder()
            .orderType(CreateSelectOrderOptionsEnum.generalForm)
            .isOrderUploadedByJudgeOrAdmin(YesOrNo.Yes)
            .orderSelectionType(ManageOrdersOptionsEnum.createAnOrder.toString())
            .otherDetails(OtherDraftOrderDetails.builder().dateCreated(LocalDateTime.now()).build()).build();
        draftOrderCollection.add(Element.<DraftOrder>builder().id(UUID.fromString(TEST_UUID))
                                     .value(draftOrder)
                                     .build());
        caseDataMap.put(DRAFT_ORDER_COLLECTION, draftOrderCollection);
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .rejectedOrdersDynamicList(DynamicList.builder()
                                                             .value(DynamicListElement.builder().code(PrlAppsConstants.TEST_UUID)
                                                                        .build())
                                                             .build()).build())
            .draftOrderCollection(draftOrderCollection)
            .build();
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(),Mockito.any()))
            .thenReturn(draftOrder);
        when(draftAnOrderService.updateDraftOrderCollection(Mockito.any(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(caseDataMap);
        Map<String, Object> response = editReturnedOrderService.updateDraftOrderCollection(caseData, authToken);
        assertTrue(response.containsKey(DRAFT_ORDER_COLLECTION));
    }

    @Ignore
    @Test
    public void  testAboutToSubmitHandlerForUploadedOrder() {
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        DraftOrder draftOrder = DraftOrder.builder()
            .orderType(CreateSelectOrderOptionsEnum.generalForm)
            .isOrderUploadedByJudgeOrAdmin(YesOrNo.Yes)
            .orderSelectionType(ManageOrdersOptionsEnum.uploadAnOrder.toString())
            .otherDetails(OtherDraftOrderDetails.builder().instructionsToLegalRepresentative("u").dateCreated(LocalDateTime.now()).build()).build();
        draftOrderCollection.add(Element.<DraftOrder>builder().id(UUID.fromString(TEST_UUID))
                                     .value(draftOrder)
                                     .build());
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(),Mockito.any()))
            .thenReturn(draftOrder);
        CaseData caseData = CaseData.builder()
            .draftOrderCollection(draftOrderCollection)
            .manageOrders(ManageOrders.builder()
                              .rejectedOrdersDynamicList(DynamicList.builder()
                                                             .value(DynamicListElement.builder().code(PrlAppsConstants.TEST_UUID)
                                                                        .build())
                                                             .build()).build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(DRAFT_ORDER_COLLECTION, List.of(Element.builder().build()));
        when(draftAnOrderService.updateDraftOrderCollection(Mockito.any(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(caseDataMap);
        Map<String, Object> response = editReturnedOrderService.updateDraftOrderCollection(caseData, authToken);
        assertTrue(response.containsKey(DRAFT_ORDER_COLLECTION));
    }

    @Test
    public void testpopulateInstructionsAndFieldsForLegalRep() {
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder()
            .value(DraftOrder.builder().otherDetails(OtherDraftOrderDetails.builder().build()).build()).build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .draftOrderCollection(draftOrderCollection)
            .manageOrders(ManageOrders.builder().rejectedOrdersDynamicList(DynamicList.builder().build()).build())
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        caseDataMap.put("orderType", "test");
        when(draftAnOrderService.populateCommonDraftOrderFields(Mockito.anyString(),Mockito.any(), Mockito.any())).thenReturn(caseDataMap);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .build();
        AboutToStartOrSubmitCallbackResponse response = editReturnedOrderService.populateInstructionsAndFieldsForLegalRep(authToken, callbackRequest);
        Assert.assertEquals("u", response.getData().get("instructionsToLegalRepresentative"));
        Assert.assertEquals("<span class='heading-h3'>General form of undertaking (N117)</span>", response.getData().get("orderName"));
    }

    @Test
    public void testPopulateInstructionsWithEmptyDraftOrderCollection() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .build();

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.populateCommonDraftOrderFields(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = editReturnedOrderService
            .populateInstructionsAndFieldsForLegalRep(authToken,callbackRequest);
        Assert.assertTrue(response.getErrors().size() > 0);
    }
}
