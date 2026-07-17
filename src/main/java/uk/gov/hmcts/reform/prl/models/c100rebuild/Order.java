package uk.gov.hmcts.reform.prl.models.c100rebuild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @CCD(ignore = true)
    private String id;
    @CCD(ignore = true)
    private String orderDetail;
    @CCD(ignore = true)
    private String caseNo;
    @CCD(ignore = true)
    private OrderDate orderDate;
    @CCD(ignore = true)
    private String currentOrder;
    @CCD(ignore = true)
    private OrderDate orderEndDate;
    @CCD(ignore = true)
    private String orderCopy;
    @CCD(ignore = true)
    private Document orderDocument;
    @CCD(ignore = true)
    private TypeOfOrderEnum typeOfOrderEnum;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Date issued", searchable = false)
  private java.time.LocalDate dateIssued;
  @CCD(label = "Date ended", searchable = false)
  private java.time.LocalDate endDate;
  @CCD(label = "Is the order current?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo orderCurrent;
  @CCD(label = "Name of Court", searchable = false)
  private String courtName;
  // ==== end synthesised definition-only fields ====
}