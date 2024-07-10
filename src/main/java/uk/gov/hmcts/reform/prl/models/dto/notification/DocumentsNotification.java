package uk.gov.hmcts.reform.prl.models.dto.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentsNotification {

    private String partyId;
    private PartyType partyType;
    private NotificationType notificationType;
    private String bulkPrintId;
    private LocalDateTime sentDateTime;
    private String remarks;
    private final List<Element<Document>> documents;

}
