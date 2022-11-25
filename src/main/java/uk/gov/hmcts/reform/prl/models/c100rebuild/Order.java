package uk.gov.hmcts.reform.prl.models.c100rebuild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private String id;
    private String orderDetail;
    private String caseNo;
    private OrderDate orderDate;
    private String currentOrder;
    private OrderDate orderEndDate;
    private String orderCopy;
    private Document orderDocument;
    private TypeOfOrderEnum typeOfOrderEnum;
}