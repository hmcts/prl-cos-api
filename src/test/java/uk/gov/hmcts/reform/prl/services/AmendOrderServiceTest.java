package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AmendOrderServiceTest {

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
    private Document stampedDocument = testDocument();
    private UUID uuid = UUID.randomUUID();
    private List<Element<OrderDetails>> orderList;
    private CaseData caseData;

    @Before
    public void setUp() throws IOException {
        originalOrder = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("filename.pdf")
            .build();
        Element<OrderDetails> orders = ElementUtils.element(uuid, OrderDetails.builder()
            .orderDocument(originalOrder)
                .dateCreated(LocalDateTime.now())
            .otherDetails(OtherOrderDetails.builder().build()).build());
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

        byte[] stampedBinaries = new byte[]{1, 2, 3, 4, 5};
        /* when(stamper.amendDocument(originalOrder, validAuth)).thenReturn(stampedBinaries);
           when(uploadDocumentService.uploadDocument(
            stampedBinaries,
            "amended_filename.pdf",
            "application/pdf",
            validAuth
        )).thenReturn(stampedDocument);*/
        when(time.now()).thenReturn(LocalDateTime.now());
        when(manageOrderService.getLoggedInUserType(Mockito.anyString())).thenReturn("");

        UserDetails userDetails = UserDetails.builder().forename("test").build();
        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);
    }

    @Test
    public void documentUpdateAndReturnedInMap() throws IOException {
        assertNotNull(amendOrderService.updateOrder(caseData, validAuth));
    }

    @Test
    public void testWantToServeOrderNo() throws IOException {
        caseData = caseData.toBuilder()
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.No).build())
            .build();
        assertNotNull(amendOrderService.updateOrder(caseData, validAuth));
    }

    @Test
    public void testWantToServeOrderNoWithData() throws IOException {
        caseData = caseData.toBuilder()
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.No)
                                .whatDoWithOrder(WhatToDoWithOrderEnum.finalizeSaveToServeLater)
                                .build())
            .build();
        assertNotNull(amendOrderService.updateOrder(caseData, validAuth));
    }

    @Test
    public void testDraftOrdeCollection() throws IOException {
        assertNotNull(amendOrderService.setDraftOrderCollection(caseData,
                                                                uk.gov.hmcts.reform.prl.models.documents.Document
                                                                   .builder().build(),
                                                                UserRoles.JUDGE.name(),"currentUserName"
        ));
    }

    @Test
    public void testDraftOrdeCollectionWithData() throws IOException {
        caseData = caseData.toBuilder()
            .draftOrderCollection(List.of(Element.<DraftOrder>builder()
                                              .value(DraftOrder.builder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                           .dateCreated(LocalDateTime.now())
                                                                                           .reviewRequiredBy(
                                                                                               AmendOrderCheckEnum.noCheck)
                                                                                           .build()).build())
                                              .build())).build();
        assertNotNull(amendOrderService.setDraftOrderCollection(caseData,
                                                                uk.gov.hmcts.reform.prl.models.documents.Document
                                                                    .builder().build(),
                                                                "","currentUserName"));
    }

    @Test
    public void testDraftOrdeCollectionWithManagerCheck() throws IOException {
        caseData = caseData.toBuilder()
            .draftOrderCollection(List.of(Element.<DraftOrder>builder()
                                              .value(DraftOrder.builder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                           .dateCreated(LocalDateTime.now())
                                                                                           .reviewRequiredBy(
                                                                                               AmendOrderCheckEnum.managerCheck)
                                                                                           .build()).build())
                                              .build())).build();
        assertNotNull(amendOrderService.setDraftOrderCollection(caseData,
                                                                uk.gov.hmcts.reform.prl.models.documents.Document
                                                                    .builder().build(),
                                                                "","currentUserName"));
    }

    @Test
    public void testDraftOrdeCollectionWithJudgeCheck() throws IOException {
        caseData = caseData.toBuilder()
            .draftOrderCollection(List.of(Element.<DraftOrder>builder()
                                              .value(DraftOrder.builder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                           .dateCreated(LocalDateTime.now())
                                                                                           .reviewRequiredBy(
                                                                                               AmendOrderCheckEnum.judgeOrLegalAdvisorCheck)
                                                                                           .build()).build())
                                              .build())).build();
        assertNotNull(amendOrderService.setDraftOrderCollection(caseData,
                                                                uk.gov.hmcts.reform.prl.models.documents.Document
                                                                    .builder().build(),
                                                                "","currentUserName"));
    }

    @Test
    public void testDraftOrdeCollectionWithManagerCheck2() throws IOException {
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
        assertNotNull(amendOrderService.setDraftOrderCollection(caseData,
                                                                uk.gov.hmcts.reform.prl.models.documents.Document
                                                                    .builder().build(),
                                                                "","currentUserName"));
    }

    @Test
    public void testDraftOrdeCollectionWithManagerCheck3() throws IOException {
        caseData = caseData.toBuilder()
            .orderCollection(orderList)
            .draftOrderCollection(List.of(Element.<DraftOrder>builder()
                                              .value(DraftOrder.builder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                           .dateCreated(LocalDateTime.now())
                                                                                           .reviewRequiredBy(
                                                                                               AmendOrderCheckEnum.managerCheck)
                                                                                           .build()).build())
                                              .build())).build();
        assertNotNull(amendOrderService.setDraftOrderCollection(caseData,
                                                                uk.gov.hmcts.reform.prl.models.documents.Document
                                                                    .builder().build(),
                                                                UserRoles.JUDGE.name(),"currentUserName"));
    }

    @Test
    public void testDraftOrdeCollectionWithManagerCheck1() throws IOException {
        Element<OrderDetails> orders = ElementUtils.element(UUID.randomUUID(), OrderDetails.builder()
            .orderDocument(originalOrder)
            .dateCreated(LocalDateTime.now())
            .otherDetails(OtherOrderDetails.builder().build()).build());
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
        assertNotNull(amendOrderService.setDraftOrderCollection(caseData,
                                                                uk.gov.hmcts.reform.prl.models.documents.Document
                                                                    .builder().build(),
                                                                "","currentUserName"));
    }

    @Test
    public void documentUpdateAndReturnedInMap1() throws IOException {

        originalOrder = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("amended_filename.pdf")
            .build();
        Element<OrderDetails> orders = ElementUtils.element(uuid, OrderDetails.builder()
            .orderDocument(originalOrder)
            .dateCreated(LocalDateTime.now())
            .otherDetails(OtherOrderDetails.builder().build()).build());
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
        binaryLink.href = randomAlphanumeric(10);
        Document.Link selfLink = new Document.Link();
        selfLink.href = randomAlphanumeric(10);

        Document.Links links = new Document.Links();
        links.binary = binaryLink;
        links.self = selfLink;

        Document document = Document.builder().build();
        document.links = links;
        document.originalDocumentName = randomAlphanumeric(10);

        return document;
    }
}
