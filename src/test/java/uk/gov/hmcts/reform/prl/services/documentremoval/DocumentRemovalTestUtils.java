package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DocumentRemovalTestUtils {

    private static final ObjectMapper OBJECT_MAPPER = CcdObjectMapper.getObjectMapper();

    private DocumentRemovalTestUtils() {
    }

    public static Map<String, Object> createTestCaseMap() {
        CaseData caseData = createTestCase();
        return OBJECT_MAPPER.convertValue(caseData, Map.class);
    }

    public static CaseData createTestCase() {
        return CaseData.builder()
            .c1ADocument(document("1", "c1a.pdf", "2007-12-03T10:15:30"))
            .orderCollection(orderCollection())
            .finalServedApplicationDetailsList(servedApplicationDetails())
            .build();
    }

    private static Document document(String documentId, String filename, String uploadTimestamp) {
        return Document.builder()
            .documentUrl("http://someUrl/" + documentId)
            .documentFileName(filename)
            .uploadTimeStamp(uploadTimestamp != null ? LocalDateTime.parse(uploadTimestamp) : null)
            .build();
    }

    private static List<Element<OrderDetails>> orderCollection() {
        return List.of(
            orderDetails("2", "order1.pdf", "2006-12-03T10:15:30"),
            orderDetails("3", "order2.pdf", "2008-12-03T10:15:30")
        );
    }

    private static Element<OrderDetails> orderDetails(String documentId, String filename, String uploadTimestamp) {
        return Element.<OrderDetails>builder()
            .id(UUID.randomUUID())
            .value(OrderDetails.builder()
                       .typeOfOrder("test")
                       .orderDocument(document(documentId, filename, uploadTimestamp))
                       .build())
            .build();
    }

    private static List<Element<ServedApplicationDetails>> servedApplicationDetails() {
        return List.of(
            servedApplicationDetails("4", "servedApp1.pdf", "2005-12-03T10:15:30", null),
            servedApplicationDetails("5", "servedApp2.pdf", "2009-12-03T10:15:30", null),
            servedApplicationDetails("6", "servedApp3.pdf", "2009-12-03T14:15:30", "2015-12-03T10:15:30")
        );
    }

    private static Element<ServedApplicationDetails> servedApplicationDetails(String documentId, String filename,
                                                                       String uploadTimestamp,
                                                                       String supportingDocumentUploadTimestamp) {
        return Element.<ServedApplicationDetails>builder()
            .id(UUID.randomUUID())
            .value(ServedApplicationDetails.builder()
                       .emailNotificationDetails(List.of(
                           emailNotificationDetails(
                               documentId,
                               filename,
                               uploadTimestamp,
                               supportingDocumentUploadTimestamp
                           )
                       ))
                       .build())
            .build();
    }

    private static Element<EmailNotificationDetails> emailNotificationDetails(String documentId, String filename,
                                                                       String uploadTimestamp,
                                                                       String supportingDocumentTimestamp) {
        return Element.<EmailNotificationDetails>builder()
            .id(UUID.randomUUID())
            .value(EmailNotificationDetails.builder()
                       .docs(List.of(
                           Element.<Document>builder()
                               .id(UUID.randomUUID())
                               .value(document(documentId, filename, uploadTimestamp))
                               .build(),
                           Element.<Document>builder()
                               .id(UUID.randomUUID())
                               .value(document("10001", "supporting-document.pdf", supportingDocumentTimestamp))
                               .build(),
                           Element.<Document>builder()
                               .id(UUID.randomUUID())
                               .value(document("10002", "supporting-document.pdf", "2006-08-03T10:15:30"))
                               .build()
                       ))
                       .build())
            .build();
    }
}
