package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.c100rebuild.Document;
import uk.gov.hmcts.reform.prl.models.c100rebuild.OrderDate;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class OtherProceedingDetails {
    private final String orderDetail;
    @JsonProperty("caseNo")
    private final String caseNumber;
    private final OrderDate orderDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate proceedingOrderDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate proceedingOrderEndDate;
    @JsonProperty("currentOrder")
    private final YesOrNo isCurrentOrder;
    private final OrderDate orderEndDate;
    private final YesOrNo orderCopy;
    private final Document orderDocument;
    private final uk.gov.hmcts.reform.prl.models.documents.Document proceedingOrderDocument;

}
