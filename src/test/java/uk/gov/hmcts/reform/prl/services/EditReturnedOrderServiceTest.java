package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;


@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class EditReturnedOrderServiceTest {

    public static final String authToken = "Bearer TestAuthToken";

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
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .draftOrderCollection(draftOrderCollection)
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
        List<Element<DraftOrder>> draftOrderCollection = List.of(Element.<DraftOrder>builder().value(DraftOrder.builder().otherDetails(
                                                                         OtherDraftOrderDetails.builder()
                                                                             .status(OrderStatusEnum.rejectedByJudge.getDisplayedValue())
                                                                             .orderCreatedByEmailId("test@gmail.com")
                                                                             .build()).build()).build());
        CaseData caseData = CaseData.builder()
            .draftOrderCollection(draftOrderCollection).build();
        assertNotNull(editReturnedOrderService.getReturnedOrdersDynamicList(testAuth, caseData));
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

        Map<String, Object> response = editReturnedOrderService.populateInstructionsAndDocuments(caseData, authToken);
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
        Map<String, Object> response = editReturnedOrderService.populateInstructionsAndDocuments(caseData, authToken);
        assertTrue(response.containsKey("editOrderTextInstructions"));
    }

    @Test
    public void  testInstructionToLegalRepresentativeWithJudgeInstructions() {
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(),Mockito.any()))
            .thenReturn(DraftOrder.builder()
                            .orderType(CreateSelectOrderOptionsEnum.generalForm)
                            .isOrderUploadedByJudgeOrAdmin(YesOrNo.Yes)
                            .otherDetails(OtherDraftOrderDetails.builder().instructionsToLegalRepresentative("u").build()).build());
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .rejectedOrdersDynamicList(DynamicList.builder()
                                                             .value(DynamicListElement.builder().code(PrlAppsConstants.TEST_UUID)
                                                                        .build())
                                                             .build()).build())
            .build();

        Map<String, Object> response = editReturnedOrderService.populateInstructionsAndDocuments(caseData, authToken);
        assertTrue(response.containsKey("instructionsToLegalRepresentative"));
    }
}
