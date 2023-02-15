package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;


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
    private final String childrenList;
    private final YesOrNo orderClosesCase;
    private final Document orderDocument;
    private final Document orderDocumentWelsh;
    private final OtherOrderDetails otherDetails;
    private final String judgeNotes;
    private final String adminNotes;
    private final ServeOrderDetails serveOrderDetails;


    @JsonIgnore
    public String getLabelForDynamicList() {

        return String.format(
            "%s - %s",
            this.orderType,
            this.getOtherDetails().getOrderCreatedDate()
        );
    }
}
