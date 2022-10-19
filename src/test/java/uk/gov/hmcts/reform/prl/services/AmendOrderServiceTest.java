package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.assertEquals;
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
    private Time time;


    @Test
    public void documentUpdateAndReturnedInMap() throws IOException {

        final String validAuth = "VALID";

        UUID uuid = UUID.randomUUID();

        uk.gov.hmcts.reform.prl.models.documents.Document originalOrder = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("filename.pdf")
            .build();
        byte[] stampedBinaries = new byte[]{1, 2, 3, 4, 5};
        Document stampedDocument = testDocument();

        Element<OrderDetails> orders = ElementUtils.element(uuid, OrderDetails.builder()
            .orderDocument(originalOrder)
            .otherDetails(OtherOrderDetails.builder().build()).build());
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        when(stamper.amendDocument(originalOrder, validAuth)).thenReturn(stampedBinaries);
        when(uploadDocumentService.uploadDocument(
            stampedBinaries,
            "amended_filename.pdf",
            "application/pdf",
            validAuth
        )).thenReturn(stampedDocument);
        when(time.now()).thenReturn(LocalDateTime.now());

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .manageOrdersDocumentToAmend(originalOrder)
                              .amendOrderDynamicList(DynamicList.builder()
                                                         .value(DynamicListElement.builder()
                                                                    .code(uuid)
                                                                    .build()).build()).build())
            .orderCollection(orderList)
            .build();

        Map<String, Object> amendedFields = Map.of("orderCollection", orderList);

        assertEquals(amendOrderService.updateOrder(caseData, validAuth), amendedFields);

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
