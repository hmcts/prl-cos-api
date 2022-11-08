package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.models.documents.Document;


@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class DraftOrder {
    private final String typeOfOrder;
    private String orderType;
    private String orderTypeId;
    private Document orderDocument;
    private OtherDraftOrderDetails otherDetails;
    private String judgeNotes;
    private String adminNotes;

    @JsonIgnore
    public String getLabelForOrdersDynamicList() {
        log.info("orderTypeId {},orderTypeId {}", this.orderType, this.orderTypeId);
        return String.format(
            "%s",
            this.orderTypeId
        );
    }
}
