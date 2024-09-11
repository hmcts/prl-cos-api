package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.notification.DocumentsNotification;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DocumentsNotifications {

    //PRL-5979 - RE7 access code cover letter
    @JsonProperty("accessCodeNotifications")
    private List<Element<DocumentsNotification>> accessCodeNotifications;
}
