package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherOrderDetails {

    @CCD(label = "Order made by", searchable = false)
    private final String createdBy;
    @CCD(label = "Date order created", searchable = false)
    private final String orderCreatedDate;
    @CCD(label = "Date order amended", searchable = false, typeOverride = FieldType.Text)
    private final LocalDateTime orderAmendedDate;
    @CCD(label = "Date order made", searchable = false)
    private final String orderMadeDate;
    @CCD(label = "Recipients", searchable = false, typeOverride = FieldType.TextArea)
    private final String orderRecipients;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Additional requirements for hearing request", searchable = false, typeOverride = FieldType.TextArea)
  private String additionalRequirementsForHearingReq;
  @CCD(label = "Order created by", searchable = false)
  private String orderCreatedBy;
  @CCD(label = "Order created by", showCondition = "orderMadeDate=\"DO_NOT_SHOW\"", searchable = false)
  private String orderCreatedByEmailId;
  @CCD(label = "Date on which the order was served", searchable = false)
  private String orderServedDate;
  @CCD(label = "Approval date", searchable = false)
  private String approvalDate;
  @CCD(label = "Order status", showCondition = "orderMadeDate=\"DO_NOT_SHOW\"", searchable = false)
  private String status;
  // ==== end synthesised definition-only fields ====
}
