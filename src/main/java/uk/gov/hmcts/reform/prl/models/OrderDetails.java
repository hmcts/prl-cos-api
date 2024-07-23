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
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.BulkPrintOrderDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OrderDetails {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime dateCreated;
    private final String withdrawnRequestType;
    private final String isWithdrawnRequestApproved;
    private final String typeOfOrder;
    private final YesOrNo doesOrderClosesCase;
    private final String orderType;
    private final String orderTypeId;
    private final YesOrNo isTheOrderAboutChildren;
    private final YesOrNo isTheOrderAboutAllChildren;
    private final String childrenList;
    private final YesOrNo orderClosesCase;
    private final Document orderDocument;
    private final Document orderDocumentWelsh;
    private final OtherOrderDetails otherDetails;
    private final String judgeNotes;
    private final String adminNotes;
    private final ServeOrderDetails serveOrderDetails;
    @JsonProperty("manageOrderHearingDetails")
    private final List<Element<HearingData>> manageOrderHearingDetails;
    //PRL-3254 - Added for storing selected hearing dropdown
    private String selectedHearingType;

    private final SdoDetails sdoDetails;
    private final YesOrNo cafcassServedOptions;
    private final String cafcassEmailId;
    private final YesOrNo cafcassCymruServedOptions;
    private final String cafcassCymruEmail;
    private final YesOrNo isOrderCreatedBySolicitor;
    private final String typeOfChildArrangementsOrder;
    //Mi compliance
    private final C21OrderOptionsEnum c21OrderOptions;
    @JsonProperty("childArrangementsOrdersToIssue")
    private final List<OrderTypeEnum> childArrangementsOrdersToIssue;
    private final ChildArrangementOrderTypeEnum selectChildArrangementsOrder;
    @JsonProperty("childOption")
    private final DynamicMultiSelectList childOption;
    @JsonProperty("isOrderUploaded")
    private final YesOrNo isOrderUploaded;

    //PRL-4225 - serve order & additional docs to other person
    @JsonProperty("bulkPrintOrderDetails")
    private List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails;

    //PENDING - personal, COMPLETED - after sos is done, NOT_REQUIRED - non-personal
    private String sosStatus;

    @JsonIgnore
    public String getLabelForDynamicList() {

        return String.format(
            "%s - %s",
            this.orderTypeId,
            this.getOtherDetails().getOrderCreatedDate()
        );
    }
}
