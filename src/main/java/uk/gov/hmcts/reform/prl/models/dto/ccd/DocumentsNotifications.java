package uk.gov.hmcts.reform.prl.models.dto.ccd;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.notification.DocumentsNotification;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class DocumentsNotifications {

    //PRL-5979 - RE7 access code cover letter
    @JsonProperty("accessCodeNotifications")
    private final List<Element<DocumentsNotification>> accessCodeNotifications;
}
