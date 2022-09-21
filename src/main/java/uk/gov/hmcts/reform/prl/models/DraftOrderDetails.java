package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;

@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class DraftOrderDetails {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime dateCreated;
    private final String orderTypeId;
    private final String orderText;
    private final Document orderDocument;

    @JsonIgnore
    public String getLabelForOrdersDynamicList() {
        log.info("orderTypeId {},dateCreated {}", this.orderTypeId, this.dateCreated);
        return String.format(
            "%s - %s",
            this.orderTypeId,
            this.orderDocument.getDocumentFileName()
        );
    }
}
