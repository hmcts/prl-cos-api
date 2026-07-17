package uk.gov.hmcts.reform.prl.models.dto.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationDetails {

    @CCD(label = "Party ID", searchable = false)
    private String partyId;
    @CCD(
            label = "Party type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "PartyType"
    )
    private PartyType partyType;
    @CCD(
            label = "Notification type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NotificationType"
    )
    private NotificationType notificationType;
    @CCD(label = "Bulk print ID", searchable = false)
    private String bulkPrintId;
    @CCD(label = "Sent date time", searchable = false)
    private LocalDateTime sentDateTime;
    @CCD(label = "Remarks", searchable = false)
    private String remarks;
}
