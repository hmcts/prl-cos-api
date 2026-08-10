package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.BulkPrintOrderDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.judicial.FinalisationDetails;

import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;


@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OrderDetails {

    @CCD(label = "Date created", showCondition = "dateCreated=\"DO_NOT_SHOW\"")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime dateCreated;
    @CCD(label = "WithDrawn / Refused / No order made?")
    private final String withdrawnRequestType;
    @CCD(label = "is request to withdrawn application approved?")
    private final String isWithdrawnRequestApproved;
    @CCD(label = "Type of order")
    private final String typeOfOrder;
    @CCD(label = "Does this order close the case?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo doesOrderClosesCase;
    @CCD(label = "Order", showCondition = "orderTypeId = \"DO_NOT_SHOW\"")
    private final String orderType;
    @CCD(label = "Order")
    private final String orderTypeId;
    @CCD(label = "Is the order about the children?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isTheOrderAboutChildren;
    @CCD(label = "Is the order about all the children?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isTheOrderAboutAllChildren;
    @CCD(label = "Children list ", searchable = false)
    private final String childrenList;
    @CCD(label = "Order closes case? ", typeOverride = FieldType.YesOrNo)
    private final YesOrNo orderClosesCase;
    @CCD(label = "English document", categoryID = "approvedOrders", searchable = false)
    private final Document orderDocument;
    @CCD(label = "Welsh document", categoryID = "approvedOrders", searchable = false)
    private final Document orderDocumentWelsh;
    @CCD(label = "Other details", searchable = false)
    private final OtherOrderDetails otherDetails;
    @CCD(label = "Notes", searchable = false)
    private final String judgeNotes;
    @CCD(label = "Court admin notes", searchable = false)
    private final String adminNotes;
    @CCD(label = "Serve order details", searchable = false)
    private final ServeOrderDetails serveOrderDetails;
    @CCD(label = " ", showCondition = "dateCreated=\"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("manageOrderHearingDetails")
    private List<Element<HearingData>> manageOrderHearingDetails;
    //PRL-3254 - Added for storing selected hearing dropdown
    @CCD(
            label = "Selected hearings dropdown value",
            showCondition = "selectedHearingType=\"DO_NOT_SHOW\"",
            searchable = false
    )
    private String selectedHearingType;

    @CCD(label = "SDO details", showCondition = "sdoDetails=\"DO_NOT_SHOW\"", searchable = false)
    private final SdoDetails sdoDetails;
    @CCD(ignore = true)
    private final YesOrNo cafcassServedOptions;
    @CCD(ignore = true)
    private final String cafcassEmailId;
    @CCD(ignore = true)
    private final YesOrNo cafcassCymruServedOptions;
    @CCD(ignore = true)
    private final String cafcassCymruEmail;
    @CCD(
            label = "Is the order created by solicitor ?",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isOrderCreatedBySolicitor;
    @CCD(label = " ", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String typeOfChildArrangementsOrder;
    //Mi compliance
    @CCD(label = "Type of c21 order", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final C21OrderOptionsEnum c21OrderOptions;
    @CCD(label = "Select orders to issue", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("childArrangementsOrdersToIssue")
    private final List<OrderTypeEnum> childArrangementsOrdersToIssue;
    @CCD(
            label = "Select type of child arrangements order",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false
    )
    private final ChildArrangementOrderTypeEnum selectChildArrangementsOrder;
    @CCD(
            label = " ",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList
    )
    @JsonProperty("childOption")
    private final DynamicMultiSelectList childOption;
    @CCD(
            label = "Order uploaded ?",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("isOrderUploaded")
    private final YesOrNo isOrderUploaded;
    @CCD(
            label = "Does order document need seal?",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo doesOrderDocumentNeedSeal;

    @CCD(
            label = " ",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo isAutoHearingReqPending;

    @CCD(label = " ", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private FinalisationDetails finalisationDetails;
    //PRL-4225 - serve order & additional docs to other person
    @CCD(label = "Bulk print order details", searchable = false)
    @JsonProperty("bulkPrintOrderDetails")
    private List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails;

    //PENDING - personal, COMPLETED - after sos is done for all respondents, NOT_REQUIRED - non-personal
    @CCD(label = "Sos status", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private String sosStatus;

    //PRL-6046 - persist FL404 order data fields
    @CCD(label = "Custom fields", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private FL404 fl404CustomFields;

    @JsonIgnore
    public String getLabelForDynamicList() {
        String date = this.getOtherDetails() != null ? this.getOtherDetails().getOrderCreatedDate() : "";
        return String.format(
            "%s - %s",
            this.orderTypeId,
            date
        );
    }
}
