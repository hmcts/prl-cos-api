package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationDetails;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespondentResponseNotificationDetails {
    private List<Element<NotificationDetails>> ap13Notifications;
    private String ap13NotificationSent;
    private List<Element<NotificationDetails>> ap14Notifications;
    private String ap14NotificationSent;
    private List<Element<NotificationDetails>> ap15Notifications;
    private String ap15NotificationSent;
    private String nameOfRespondentAp13;
    private String nameOfRespondentAp14;
    private String nameOfRespondentAp15;

}
