package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmendOrderServiceTest {

    @InjectMocks
    private AmendOrderService amendOrderService;

    @Mock
    private AmendedOrderStamper stamper;

    @Mock
    private UploadDocumentService uploadDocumentService;

    @Mock
    private ManageOrderService manageOrderService;

    @Mock
    private UserService userService;

    @Mock
    private Time time;

    private final String validAuth = "VALID";
    private uk.gov.hmcts.reform.prl.models.documents.Document originalOrder;
    private final UUID uuid = randomUUID();
    private List<Element<OrderDetails>> orderList;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        originalOrder = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("filename.pdf")
            .build();
        Element<OrderDetails> orders = ElementUtils.element(
            uuid, OrderDetails.builder()
                .orderDocument(originalOrder)
                .dateCreated(LocalDateTime.now())
                .otherDetails(OtherOrderDetails.builder().build()).build()
        );
        orderList = new ArrayList<>();
        orderList.add(orders);
        caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .manageOrdersDocumentToAmend(originalOrder)
                              .manageOrdersAmendedOrder(originalOrder)
                              .amendOrderSelectCheckOptions(AmendOrderCheckEnum.noCheck)
                              .amendOrderDynamicList(DynamicList.builder()
                                                         .value(DynamicListElement.builder()
                                                                    .code(uuid)
                                                                    .build()).build()).build())
            .orderCollection(orderList)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .build();

        when(time.now()).thenReturn(LocalDateTime.now());
        when(manageOrderService.getLoggedInUserType(Mockito.anyString())).thenReturn("");

        UserDetails userDetails = UserDetails.builder().forename("test").build();
        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);
    }

    @Test
    void documentUpdateAndReturnedInMap() {
        assertNotNull(amendOrderService.updateOrder(caseData, validAuth));
    }

    @Test
    void testWantToServeOrderNo() {
        caseData = caseData.toBuilder()
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.No).build())
            .build();
        assertNotNull(amendOrderService.updateOrder(caseData, validAuth));
    }

    @Test
    void testWantToServeOrderNoWithData() {
        caseData = caseData.toBuilder()
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.No)
                                .whatDoWithOrder(WhatToDoWithOrderEnum.finalizeSaveToServeLater)
                                .build())
            .build();
        assertNotNull(amendOrderService.updateOrder(caseData, validAuth));
    }

    @Test
    void testDraftOrderCollection() {
        assertNotNull(amendOrderService.setDraftOrderCollection(
            caseData,
            uk.gov.hmcts.reform.prl.models.documents.Document
                .builder().build(),
            UserRoles.JUDGE.name(), "currentUserName"
        ));
    }

    @Test
    void testDraftOrderCollectionWithData() {
        caseData = caseData.toBuilder()
            .draftOrderCollection(List.of(Element.<DraftOrder>builder()
                                              .value(DraftOrder.builder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                           .dateCreated(LocalDateTime.now())
                                                                                           .reviewRequiredBy(
                                                                                               AmendOrderCheckEnum.noCheck)
                                                                                           .build()).build())
                                              .build())).build();
        assertNotNull(amendOrderService.setDraftOrderCollection(
            caseData,
            uk.gov.hmcts.reform.prl.models.documents.Document
                .builder().build(),
            "", "currentUserName"
        ));
    }

    @Test
    void testDraftOrderCollectionWithManagerCheck() {
        caseData = caseData.toBuilder()
            .draftOrderCollection(List.of(Element.<DraftOrder>builder()
                                              .value(DraftOrder.builder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                           .dateCreated(LocalDateTime.now())
                                                                                           .reviewRequiredBy(
                                                                                               AmendOrderCheckEnum.managerCheck)
                                                                                           .build()).build())
                                              .build())).build();
        assertNotNull(amendOrderService.setDraftOrderCollection(
            caseData,
            uk.gov.hmcts.reform.prl.models.documents.Document
                .builder().build(),
            "", "currentUserName"
        ));
    }

    @Test
    void testDraftOrderCollectionWithJudgeCheck() {
        caseData = caseData.toBuilder()
            .draftOrderCollection(List.of(Element.<DraftOrder>builder()
                                              .value(DraftOrder.builder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                           .dateCreated(LocalDateTime.now())
                                                                                           .reviewRequiredBy(
                                                                                               AmendOrderCheckEnum.judgeOrLegalAdvisorCheck)
                                                                                           .build()).build())
                                              .build())).build();
        assertNotNull(amendOrderService.setDraftOrderCollection(
            caseData,
            uk.gov.hmcts.reform.prl.models.documents.Document
                .builder().build(),
            "", "currentUserName"
        ));
    }

    @Test
    void testDraftOrderCollectionWithManagerCheck2() {
        caseData = caseData.toBuilder()
            .orderCollection(orderList)
            .draftOrderCollection(List.of(Element.<DraftOrder>builder()
                                              .value(DraftOrder.builder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                           .dateCreated(LocalDateTime.now())
                                                                                           .reviewRequiredBy(
                                                                                               AmendOrderCheckEnum.managerCheck)
                                                                                           .build()).build())
                                              .build()))
            .manageOrders(ManageOrders.builder()
                              .manageOrdersDocumentToAmend(originalOrder)
                              .amendOrderSelectCheckOptions(AmendOrderCheckEnum.managerCheck)
                              .amendOrderDynamicList(DynamicList.builder()
                                                         .value(DynamicListElement.builder()
                                                                    .code(uuid)
                                                                    .build()).build()).build())
            .build();
        assertNotNull(amendOrderService.setDraftOrderCollection(
            caseData,
            uk.gov.hmcts.reform.prl.models.documents.Document
                .builder().build(),
            "", "currentUserName"
        ));
    }

    @Test
    void testDraftOrderCollectionWithManagerCheck3() {
        caseData = caseData.toBuilder()
            .orderCollection(orderList)
            .draftOrderCollection(List.of(Element.<DraftOrder>builder()
                                              .value(DraftOrder.builder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                           .dateCreated(LocalDateTime.now())
                                                                                           .reviewRequiredBy(
                                                                                               AmendOrderCheckEnum.managerCheck)
                                                                                           .build()).build())
                                              .build())).build();
        assertNotNull(amendOrderService.setDraftOrderCollection(
            caseData,
            uk.gov.hmcts.reform.prl.models.documents.Document
                .builder().build(),
            UserRoles.JUDGE.name(), "currentUserName"
        ));
    }

    @Test
    void testDraftOrderCollectionWithManagerCheck1() {
        Element<OrderDetails> orders = ElementUtils.element(
            randomUUID(), OrderDetails.builder()
                .orderDocument(originalOrder)
                .dateCreated(LocalDateTime.now())
                .otherDetails(OtherOrderDetails.builder().build()).build()
        );
        orderList = new ArrayList<>();
        orderList.add(orders);
        caseData = caseData.toBuilder()
            .orderCollection(orderList)
            .draftOrderCollection(List.of(Element.<DraftOrder>builder()
                                              .value(DraftOrder.builder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                           .dateCreated(LocalDateTime.now())
                                                                                           .reviewRequiredBy(
                                                                                               AmendOrderCheckEnum.managerCheck)
                                                                                           .build()).build())
                                              .build())).build();
        assertNotNull(amendOrderService.setDraftOrderCollection(
            caseData,
            uk.gov.hmcts.reform.prl.models.documents.Document
                .builder().build(),
            "", "currentUserName"
        ));
    }

    @Test
    void documentUpdateAndReturnedInMap1() {

        originalOrder = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("amended_filename.pdf")
            .build();
        Element<OrderDetails> orders = ElementUtils.element(
            uuid, OrderDetails.builder()
                .orderDocument(originalOrder)
                .dateCreated(LocalDateTime.now())
                .otherDetails(OtherOrderDetails.builder().build()).build()
        );
        orderList = new ArrayList<>();
        orderList.add(orders);
        caseData = caseData.toBuilder()
            .orderCollection(orderList)
            .manageOrders(caseData.getManageOrders().toBuilder().currentOrderCreatedDateTime(LocalDateTime.now()).build())
            .build();
        assertNotNull(amendOrderService.updateOrder(caseData, validAuth));
    }

    public static Document testDocument() {
        Document.Link binaryLink = new Document.Link();
        binaryLink.href = randomUUID().toString().replace("-", "").substring(0, 10);
        Document.Link selfLink = new Document.Link();
        selfLink.href = randomUUID().toString().replace("-", "").substring(0, 10);

        Document.Links links = new Document.Links();
        links.binary = binaryLink;
        links.self = selfLink;

        Document document = Document.builder().build();
        document.links = links;
        document.originalDocumentName = randomUUID().toString().replace("-", "").substring(0, 10);

        return document;
    }
}
