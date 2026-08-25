package uk.gov.hmcts.reform.prl.mapper.bundle;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMM_YYYY;

final class BundleOrderDocumentNameHelper {

    private static final DateTimeFormatter ORDER_MADE_DATE_FORMATTER = DateTimeFormatter.ofPattern(
        D_MMM_YYYY,
        Locale.ENGLISH
    );
    private static final DateTimeFormatter BUNDLE_INDEX_ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern(
        "dd-MM-yy",
        Locale.ENGLISH
    );

    private BundleOrderDocumentNameHelper() {
    }

    static String getBundleIndexOrderTitle(Document document, OrderDetails orderDetails) {
        String orderTitle = document.getDocumentFileName();

        return Optional.ofNullable(orderDetails)
            .map(OrderDetails::getOtherDetails)
            .map(OtherOrderDetails::getOrderMadeDate)
            .map(BundleOrderDocumentNameHelper::formatOrderMadeDateForBundleIndex)
            .filter(StringUtils::isNotBlank)
            .map(orderMadeDate -> orderTitle + " : " + orderMadeDate)
            .orElse(orderTitle);
    }

    static String getBundleIndexOrderTitle(Document document, Map<String, String> orderDocumentTitles) {
        if (document == null || orderDocumentTitles == null) {
            return document == null ? null : document.getDocumentFileName();
        }

        return Optional.ofNullable(orderDocumentTitles.get(document.getDocumentUrl()))
            .or(() -> Optional.ofNullable(orderDocumentTitles.get(document.getDocumentBinaryUrl())))
            .orElse(document.getDocumentFileName());
    }

    static Map<String, String> getOrderDocumentTitles(List<Element<OrderDetails>> orderCollection) {
        Map<String, String> documentTitles = new HashMap<>();
        if (orderCollection == null) {
            return documentTitles;
        }

        orderCollection.stream()
            .filter(orderedDetailsElement -> orderedDetailsElement != null && orderedDetailsElement.getValue() != null)
            .map(Element::getValue)
            .forEach(orderDetails -> {
                addOrderDocumentTitle(documentTitles, orderDetails.getOrderDocument(), orderDetails);
                addOrderDocumentTitle(documentTitles, orderDetails.getOrderDocumentWelsh(), orderDetails);
            });
        return documentTitles;
    }

    private static void addOrderDocumentTitle(Map<String, String> documentTitles, Document document, OrderDetails orderDetails) {
        if (document == null) {
            return;
        }

        String documentTitle = getBundleIndexOrderTitle(document, orderDetails);
        if (StringUtils.isNotBlank(document.getDocumentUrl())) {
            documentTitles.put(document.getDocumentUrl(), documentTitle);
        }
        if (StringUtils.isNotBlank(document.getDocumentBinaryUrl())) {
            documentTitles.put(document.getDocumentBinaryUrl(), documentTitle);
        }
    }

    private static String formatOrderMadeDateForBundleIndex(String orderMadeDate) {
        try {
            return LocalDate.parse(orderMadeDate, ORDER_MADE_DATE_FORMATTER).format(BUNDLE_INDEX_ORDER_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
