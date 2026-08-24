package uk.gov.hmcts.reform.prl.mapper.bundle;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BundleOrderDocumentNameHelperTest {

    @Test
    void getBundleIndexOrderTitleReturnsNullWhenDocumentIsNull() {
        assertNull(BundleOrderDocumentNameHelper.getBundleIndexOrderTitle(null, Map.of()));
    }

    @Test
    void getBundleIndexOrderTitleReturnsDocumentFileNameWhenDocumentTitleMapIsNull() {
        Document document = Document.builder()
            .documentFileName("order.pdf")
            .build();

        assertEquals(
            "order.pdf",
            BundleOrderDocumentNameHelper.getBundleIndexOrderTitle(document, (Map<String, String>) null)
        );
    }

    @Test
    void getBundleIndexOrderTitleFallsBackToBinaryUrlTitle() {
        Document document = Document.builder()
            .documentUrl("document-url")
            .documentBinaryUrl("binary-url")
            .documentFileName("order.pdf")
            .build();
        Map<String, String> orderDocumentTitles = new HashMap<>();
        orderDocumentTitles.put("binary-url", "Order title 24-08-26");

        assertEquals("Order title 24-08-26", BundleOrderDocumentNameHelper.getBundleIndexOrderTitle(
            document,
            orderDocumentTitles
        ));
    }

    @Test
    void getOrderDocumentTitlesIgnoresNullElementsNullValuesAndDocumentsWithBlankUrls() {
        Document documentWithBlankUrls = Document.builder()
            .documentUrl("")
            .documentBinaryUrl(" ")
            .documentFileName("blank-url-order.pdf")
            .build();
        OrderDetails orderDetails = OrderDetails.builder()
            .orderDocument(documentWithBlankUrls)
            .orderDocumentWelsh(null)
            .otherDetails(OtherOrderDetails.builder()
                              .orderMadeDate("24 Aug 2026")
                              .build())
            .build();

        Map<String, String> result = BundleOrderDocumentNameHelper.getOrderDocumentTitles(Arrays.asList(
            Element.<OrderDetails>builder().id(UUID.randomUUID()).value(null).build(),
            Element.<OrderDetails>builder().id(UUID.randomUUID()).value(orderDetails).build()
        ));

        assertTrue(result.isEmpty());
    }

    @Test
    void getOrderDocumentTitlesAddsTitlesForDocumentAndWelshDocumentUrls() {
        Document englishDocument = Document.builder()
            .documentUrl("english-url")
            .documentBinaryUrl("english-binary-url")
            .documentFileName("English_Order.pdf")
            .build();
        Document welshDocument = Document.builder()
            .documentUrl("welsh-url")
            .documentBinaryUrl("welsh-binary-url")
            .documentFileName("Welsh_English_Order.pdf")
            .build();
        OrderDetails orderDetails = OrderDetails.builder()
            .orderDocument(englishDocument)
            .orderDocumentWelsh(welshDocument)
            .otherDetails(OtherOrderDetails.builder()
                              .orderMadeDate("24 Aug 2026")
                              .build())
            .build();

        Map<String, String> result = BundleOrderDocumentNameHelper.getOrderDocumentTitles(Arrays.asList(
            null,
            Element.<OrderDetails>builder().id(UUID.randomUUID()).value(orderDetails).build()
        ));

        assertEquals("English_Order.pdf 24-08-26", result.get("english-url"));
        assertEquals("English_Order.pdf 24-08-26", result.get("english-binary-url"));
        assertEquals("Welsh_English_Order.pdf 24-08-26", result.get("welsh-url"));
        assertEquals("Welsh_English_Order.pdf 24-08-26", result.get("welsh-binary-url"));
    }

    @Test
    void getBundleIndexOrderTitleReturnsDocumentFileNameWhenOrderMadeDateCannotBeParsed() {
        Document document = Document.builder()
            .documentFileName("order.pdf")
            .build();
        OrderDetails orderDetails = OrderDetails.builder()
            .otherDetails(OtherOrderDetails.builder()
                              .orderMadeDate("not a date")
                              .build())
            .build();

        assertEquals("order.pdf", BundleOrderDocumentNameHelper.getBundleIndexOrderTitle(document, orderDetails));
    }
}
