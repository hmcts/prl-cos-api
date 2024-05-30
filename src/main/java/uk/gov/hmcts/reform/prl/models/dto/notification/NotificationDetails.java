package uk.gov.hmcts.reform.prl.models.dto.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationDetails {

    private String partyId;
    private PartyType partyType;
    private NotificationType notificationType;
    private String bulkPrintId;
    private LocalDateTime sentDateTime;
    private String remarks;
}
