package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationDetails;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FM5ReminderNotificationDetails {

    @CCD(
            label = "FM5 reminder notifications",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class, CaseworkerPrivatelawCourtadminRAccess.class, CitizenRAccess.class}
    )
    private List<Element<NotificationDetails>> fm5ReminderNotifications;
    @CCD(
            label = "FM5 reminders sent?",
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserRAccess.class, CitizenRAccess.class}
    )
    private String fm5RemindersSent;
}
