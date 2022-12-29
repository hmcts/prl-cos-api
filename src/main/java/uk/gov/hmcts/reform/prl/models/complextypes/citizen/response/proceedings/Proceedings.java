package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.c100rebuild.OrderDate;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Data
@Builder(toBuilder = true)
public class Proceedings {
    private final String orderType;
    private final List<OtherProceedingDetails> proceedingDetails;

    public List<OtherProceedingDetails> getProceedingDetails() {
        for (OtherProceedingDetails proceedingDetails: proceedingDetails
             ) {
            proceedingDetails.toBuilder()
                .proceedingOrderDate(buildDate(proceedingDetails.getOrderDate()))
                .proceedingOrderEndDate(buildDate(proceedingDetails.getOrderEndDate()))
                .proceedingOrderDocument(buildDocument(proceedingDetails.getOrderDocument()))
                .build();
        }
        return proceedingDetails;
    }

    private static LocalDate buildDate(OrderDate date) {
        if (isNotEmpty(date.getYear()) &&  isNotEmpty(date.getMonth()) && isNotEmpty(date.getDay())) {
            return LocalDate.of(Integer.parseInt(date.getYear()), Integer.parseInt(date.getMonth()),
                                Integer.parseInt(date.getDay()));
        }
        return null;
    }

    public static Document buildDocument(uk.gov.hmcts.reform.prl.models.c100rebuild.Document orderDocument) {
        if (isNotEmpty(orderDocument)) {
            return Document.builder()
                .documentUrl(orderDocument.getUrl())
                .documentBinaryUrl(orderDocument.getBinaryUrl())
                .documentFileName(orderDocument.getFilename())
                .build();
        }
        return null;
    }
}
