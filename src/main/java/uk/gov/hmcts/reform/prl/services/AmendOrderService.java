package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.time.Time;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendOrderService {
    private static final String MEDIA_TYPE = "application/pdf";
    private static final String FILE_NAME_PREFIX = "amended_";

    private final AmendedOrderStamper stamper;
    private final  UploadDocumentService uploadService;
    private final Time time;

    public Map<String, Object> updateOrder(CaseData caseData, String authorisation) {
        ManageOrders eventData = caseData.getManageOrders();

        byte[] stampedBinaries = stamper.amendDocument(eventData.getManageOrdersDocumentToAmend(), authorisation);
        String amendedFileName = updateFileName(eventData.getManageOrdersDocumentToAmend());
        Document stampedDocument = uploadService.uploadDocument(stampedBinaries, amendedFileName, MEDIA_TYPE, authorisation);


        uk.gov.hmcts.reform.prl.models.documents.Document updatedDocument = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName(stampedDocument.originalDocumentName)
            .documentUrl(stampedDocument.links.self.href)
            .documentBinaryUrl(stampedDocument.links.binary.href)
            .build();


        return updateAmendedOrderDetails(caseData, updatedDocument);

    }

    private String updateFileName(uk.gov.hmcts.reform.prl.models.documents.Document original) {
        String filename = original.getDocumentFileName();
        return filename.startsWith(FILE_NAME_PREFIX) ? filename : FILE_NAME_PREFIX + filename;
    }

    private Map<String, Object> updateAmendedOrderDetails(CaseData caseData,
                                                          uk.gov.hmcts.reform.prl.models.documents.Document amendedDocument) {

        UUID selectedOrderId = caseData.getManageOrders().getAmendOrderDynamicList().getValueCodeAsUuid();
        List<Element<OrderDetails>> orders = caseData.getOrderCollection();

        orders.stream()
            .filter(order -> Objects.equals(order.getId(), selectedOrderId))
            .findFirst()
            .ifPresent(order -> {
                OrderDetails amended = order.getValue().toBuilder()
                    .orderDocument(amendedDocument)
                    .otherDetails(order.getValue().getOtherDetails().toBuilder()
                                      .orderAmendedDate(time.now().toLocalDate())
                                      .build())
                    .build();

                orders.set(orders.indexOf(order), element(order.getId(), amended));
            });
        return Map.of("orderCollection", orders);

    }
}
