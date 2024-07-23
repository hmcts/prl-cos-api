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

    private final String partyId;
    private final PartyType partyType;
    private final NotificationType notificationType;
    private final String bulkPrintId;
    private final LocalDateTime sentDateTime;
    private final String remarks;
}
