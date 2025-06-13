package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RemoveDraftOrderFields;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.RemoveDraftOrderService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
@PropertySource(value = "classpath:application.yaml")
class RemoveDraftOrderControllerTest {

    @InjectMocks
    private RemoveDraftOrderController removeDraftOrderController;

    @Mock
    private  ObjectMapper objectMapper;

    @Mock
    private  RemoveDraftOrderService removeDraftOrderService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private UserService userService;

    @Mock
    ElementUtils elementUtils;

    private CaseData caseData;
    private List<Element<DraftOrder>> draftOrderCollection;

    private static final String AUTH_TOKEN = "Bearer TestAuthToken";
    private static final String S2S_TOKEN = "s2s AuthToken";
    private static final String REMOVE_DRAFT_ORDERS_DYNAMIC_LIST = "removeDraftOrdersDynamicList";




    @BeforeEach
    void setUp() {

        draftOrderCollection = List.of(Element.<DraftOrder>builder()
             .id(UUID.randomUUID())
             .value(DraftOrder.builder().otherDetails(
                 OtherDraftOrderDetails.builder()
                     .dateCreated(LocalDateTime.now())
                     .status(OrderStatusEnum.rejectedByJudge.getDisplayedValue())
                     .orderCreatedByEmailId("test@gmail.com")
                     .build()).build()).build(),
         Element.<DraftOrder>builder()
             .id(UUID.randomUUID())
             .value(DraftOrder.builder().otherDetails(
                 OtherDraftOrderDetails.builder()
                     .dateCreated(LocalDateTime.now())
                     .status(OrderStatusEnum.draftedByLR.getDisplayedValue())
                     .orderCreatedByEmailId("test1@gmail.com")
                     .build()).build()).build());

        caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .removeDraftOrderFields(RemoveDraftOrderFields.builder()
                                        .removeDraftOrderText("Draft removed").build())
            .build();
    }

    @Test
    void testRemoveDraftOrderDropDown() {

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(REMOVE_DRAFT_ORDERS_DYNAMIC_LIST, ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(Event.REMOVE_DRAFT_ORDER.getId())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(removeDraftOrderService.getDraftOrderDynamicList(caseData, callbackRequest.getEventId(), AUTH_TOKEN)).thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = removeDraftOrderController
            .generateRemoveDraftOrderDropDown(AUTH_TOKEN,S2S_TOKEN, callbackRequest);
        assertNotNull(response);
        assertNotNull(response.getData());
        DynamicList supportedDraftOrderList = (DynamicList) response.getData().get(REMOVE_DRAFT_ORDERS_DYNAMIC_LIST);
        assertEquals(2, supportedDraftOrderList.getListItems().size());
    }

    @Test
    void testRemoveDraftOrderDropDownWithDraftOrderCollectionIsNull() {

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(REMOVE_DRAFT_ORDERS_DYNAMIC_LIST, ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CaseData caseData1 = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        Map<String, Object> stringObjectMap = caseData1.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(Event.REMOVE_DRAFT_ORDER.getId())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData1);
        AboutToStartOrSubmitCallbackResponse response = removeDraftOrderController
            .generateRemoveDraftOrderDropDown(AUTH_TOKEN,S2S_TOKEN, callbackRequest);
        assertNotNull(response);
        assertNotNull(response.getErrors());
        assertEquals("There are no draft orders", response.getErrors().getFirst());
    }

    @Test
    void testRemoveDraftOrderDropDownAndAuthorizationFalse() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(Event.REMOVE_DRAFT_ORDER.getId())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);

        RuntimeException exception =  assertThrows(RuntimeException.class, () ->
            removeDraftOrderController.generateRemoveDraftOrderDropDown(AUTH_TOKEN,S2S_TOKEN, callbackRequest));

        assertEquals("Invalid Client", exception.getMessage());
    }

    @Test
    void testHandleRemoveDraftOrderAboutToSubmitted() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(Event.REMOVE_DRAFT_ORDER.getId())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        List<Element<DraftOrder>> returnDraftOrderCollection = List.of(Element.<DraftOrder>builder()
            .id(UUID.fromString(PrlAppsConstants.TEST_UUID))
            .value(DraftOrder.builder().otherDetails(
                OtherDraftOrderDetails.builder()
                    .dateCreated(LocalDateTime.now())
                    .status(OrderStatusEnum.rejectedByJudge.getDisplayedValue())
                    .orderCreatedByEmailId("test@gmail.com")
            .build()).build()).build());

        when(removeDraftOrderService.removeSelectedDraftOrder(caseData)).thenReturn(returnDraftOrderCollection);
        AboutToStartOrSubmitCallbackResponse response = removeDraftOrderController
            .handleRemoveDraftOrderAboutToSubmitted(AUTH_TOKEN,S2S_TOKEN, callbackRequest);
        assertNotNull(response);
        assertNotNull(response.getData());
        List<Element<DraftOrder>> returnDraftOrderList = (List<Element<DraftOrder>>) response.getData()
            .get(RemoveDraftOrderController.DRAFT_ORDER_COLLECTION);
        assertEquals(1, returnDraftOrderList.size());
        assertEquals(UUID.fromString(PrlAppsConstants.TEST_UUID), returnDraftOrderList.getFirst().getId());
        assertEquals("Draft removed",  response.getData().get(RemoveDraftOrderController.REMOVED_DRAFT_ORDER_TEXT));
    }

    @Test
    void testHandleRemoveDraftOrderAboutToSubmittedAndAuthorizationFalse() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(Event.REMOVE_DRAFT_ORDER.getId())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);

        RuntimeException exception =  assertThrows(RuntimeException.class, () ->
            removeDraftOrderController.handleRemoveDraftOrderAboutToSubmitted(AUTH_TOKEN,S2S_TOKEN, callbackRequest));

        assertEquals("Invalid Client", exception.getMessage());
    }
}
