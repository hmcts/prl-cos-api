package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;

@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OrderDetails {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime dateCreated;
    private final String orderType;
    private final String orderTypeId;
    private final Document orderDocument;
    private final OtherOrderDetails otherDetails;

    @JsonIgnore
    public String getLabelForDynamicList() {

        return String.format(
            "%s - %s",
            this.orderType,
            this.getOtherDetails().getOrderCreatedDate()
        );
    }

    @JsonIgnore
    public String getLabelForOrdersDynamicList() {
        log.info("orderType {}, orderTypeId {},dateCreated {}",this.orderType,this.orderTypeId,this.dateCreated);
        return String.format(
            "%s - %s",
            this.orderType,
            this.orderDocument.getDocumentFileName()
        );
    }
}
