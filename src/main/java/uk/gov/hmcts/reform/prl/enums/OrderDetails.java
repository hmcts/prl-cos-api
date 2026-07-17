package uk.gov.hmcts.reform.prl.enums;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.manageorder.ServeOrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.SdoDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildArrangementOrderTypeEnum2;
import uk.gov.hmcts.reform.prl.models.dto.ccd.BulkPrintOrderDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.FL400;
import uk.gov.hmcts.reform.prl.models.dto.judicial.FinalisationDetails;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OrderDetails {

    @CCD(label = "Date created", showCondition = "dateCreated=\"DO_NOT_SHOW\"")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime dateCreated;
    @CCD(label = "Order", showCondition = "orderTypeId = \"DO_NOT_SHOW\"")
    private final String orderType;
    @CCD(label = "English document", categoryID = "approvedOrders", searchable = false)
    private final Document orderDocument;
    @CCD(label = "Other details", searchable = false)
    private final OtherOrderDetails otherDetails;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Order uploaded ?", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isOrderUploaded;
  @CCD(label = "Does order document need seal?", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo doesOrderDocumentNeedSeal;
  @CCD(label = "Order")
  private String orderTypeId;
  @CCD(label = "WithDrawn / Refused / No order made?")
  private String withdrawnRequestType;
  @CCD(label = "is request to withdrawn application approved?")
  private String isWithdrawnRequestApproved;
  @CCD(label = "Type of order")
  private String typeOfOrder;
  @CCD(label = "Does this order close the case?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo doesOrderClosesCase;
  @CCD(label = "Welsh document", categoryID = "approvedOrders", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.Document orderDocumentWelsh;
  @CCD(label = "Order closes case? ")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo orderClosesCase;
  @CCD(label = "Children list ", searchable = false)
  private String childrenList;
  @CCD(label = "Is the order about the children?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isTheOrderAboutChildren;
  @CCD(label = "Is the order about all the children?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isTheOrderAboutAllChildren;
  @CCD(label = "Serve order details", searchable = false)
  private ServeOrderDetails serveOrderDetails;
  @CCD(label = " ", showCondition = "dateCreated=\"DO_NOT_SHOW\"", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<HearingData>> manageOrderHearingDetails;
  @CCD(label = "Notes", searchable = false)
  private String judgeNotes;
  @CCD(label = "Court admin notes", searchable = false)
  private String adminNotes;
  @CCD(label = "SDO details", showCondition = "sdoDetails=\"DO_NOT_SHOW\"", searchable = false)
  private SdoDetails sdoDetails;
  @CCD(
          label = "Selected hearings dropdown value",
          showCondition = "selectedHearingType=\"DO_NOT_SHOW\"",
          searchable = false
  )
  private String selectedHearingType;
  @CCD(
          label = "Is the order created by solicitor ?",
          showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
          searchable = false
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isOrderCreatedBySolicitor;
  @CCD(label = " ", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
  private String typeOfChildArrangementsOrder;
  @CCD(label = "Type of c21 order", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
  private C21OrderOptionsEnum c21OrderOptions;
  @CCD(label = "Select orders to issue", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
  private java.util.Set<OrderTypeEnum> childArrangementsOrdersToIssue;
  @CCD(
          label = "Select type of child arrangements order",
          showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
          searchable = false
  )
  private ChildArrangementOrderTypeEnum2 selectChildArrangementsOrder;
  @CCD(
          label = " ",
          showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
          searchable = false,
          typeOverride = FieldType.DynamicMultiSelectList
  )
  private String childOption;
  @CCD(label = "Bulk print order details", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<BulkPrintOrderDetail>> bulkPrintOrderDetails;
  @CCD(label = " ", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isAutoHearingReqPending;
  @CCD(label = "Sos status", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
  private String sosStatus;
  @CCD(label = "Custom fields", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
  private FL400 fl404CustomFields;
  @CCD(label = " ", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
  private FinalisationDetails finalisationDetails;
  // ==== end synthesised definition-only fields ====
}
