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
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertTrue;


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

    private static final String testAuth = "auth";

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
                                                                             .build()).build()).build());
        CaseData caseData = CaseData.builder()
            .draftOrderCollection(draftOrderCollection).build();
        Map<String, Object> response = editReturnedOrderService.getReturnedOrdersDynamicList(testAuth, caseData);
        assertTrue(response.containsKey("rejectedOrdersDynamicList"));
    }

    @Test
    public void testInstructionToLegalRepresentative() {
        List<Element<DraftOrder>> draftOrderCollection = List.of(Element.<DraftOrder>builder()
                                                                     .id(UUID.fromString(PrlAppsConstants.TEST_UUID))
                                                                     .value(DraftOrder.builder().otherDetails(
            OtherDraftOrderDetails.builder()
                .status(OrderStatusEnum.rejectedByJudge.getDisplayedValue())
                .orderCreatedByEmailId("test@gmail.com")
                .instructionsToLegalRepresentative("instructions")
                .build()).build()).build());
        CaseData caseData = CaseData.builder()
            .draftOrderCollection(draftOrderCollection)
            .manageOrders(ManageOrders.builder()
                              .rejectedOrdersDynamicList(DynamicList.builder()
                                                             .value(DynamicListElement.builder().code(PrlAppsConstants.TEST_UUID)
                                                                        .build())
                                                             .build()).build())
            .build();
        Map<String, Object> response = editReturnedOrderService.populateInstructionsAndDocuments(caseData);
        assertTrue(response.containsKey("instructionsToLegalRepresentative"));
    }

    @Test
    public void testInstructionToLegalRepresentativeElseCondition() {
        List<Element<DraftOrder>> draftOrderCollection = List.of(Element.<DraftOrder>builder()
                                                                     .id(UUID.fromString(PrlAppsConstants.TEST_UUID))
                                                                     .value(DraftOrder.builder()
                                                                                .isOrderUploadedByJudgeOrAdmin(YesOrNo.Yes)
                                                                                .otherDetails(
                                                                         OtherDraftOrderDetails.builder()
                                                                             .status(OrderStatusEnum.rejectedByJudge.getDisplayedValue())
                                                                             .orderCreatedByEmailId("test@gmail.com")
                                                                             .build()).build()).build());
        CaseData caseData = CaseData.builder()
            .draftOrderCollection(draftOrderCollection)
            .manageOrders(ManageOrders.builder()
                              .rejectedOrdersDynamicList(DynamicList.builder()
                                                             .value(DynamicListElement.builder().code(PrlAppsConstants.TEST_UUID)
                                                                        .build())
                                                             .build()).build())
            .build();
        Map<String, Object> response = editReturnedOrderService.populateInstructionsAndDocuments(caseData);
        assertTrue(response.containsKey("editOrderTextInstructions"));
    }
}
